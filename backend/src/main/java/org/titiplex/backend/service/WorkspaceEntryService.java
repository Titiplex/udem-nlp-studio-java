package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.RuleStore;
import org.titiplex.backend.store.WorkspaceEntryStore;
import org.titiplex.backend.store.jdbc.JdbcWorkspaceEntryStore;
import org.titiplex.io.RawTextReader;
import org.titiplex.io.YamlRuleLoader;
import org.titiplex.model.CorrectionEntry;
import org.titiplex.model.RawBlock;
import org.titiplex.pipeline.ConlluPipeline;
import org.titiplex.rules.CorrectionRule;
import org.titiplex.rules.RuleEngine;
import org.titiplex.service.ParityService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceEntryService {

    private final ProjectContextService projectContextService;
    private final WorkspaceEntryStore workspaceEntryStore;
    private final RuleStore ruleStore;
    private final AnnotationConfigComposerService annotationConfigComposerService;

    public WorkspaceEntryService(ProjectContextService projectContextService,
                                 WorkspaceEntryStore workspaceEntryStore,
                                 RuleStore ruleStore,
                                 AnnotationConfigComposerService annotationConfigComposerService) {
        this.projectContextService = projectContextService;
        this.workspaceEntryStore = workspaceEntryStore;
        this.ruleStore = ruleStore;
        this.annotationConfigComposerService = annotationConfigComposerService;
    }

    public List<EntrySummaryDto> listEntries() {
        return workspaceEntryStore.listEntries(projectContextService.getRequiredActiveContext());
    }

    public EntryDetailDto getEntry(UUID id) {
        return workspaceEntryStore.getEntry(projectContextService.getRequiredActiveContext(), id);
    }

    public EntryDetailDto saveEntry(EntryDetailDto dto) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        return workspaceEntryStore.saveEntry(project, dto, SaveOptions.standard(project.localMember().principalId()));
    }

    public EntryDetailDto runCorrection(CorrectionRunRequestDto request) {
        if (request.entryId() == null) {
            throw new IllegalArgumentException("entryId cannot be null");
        }

        ProjectContext project = projectContextService.getRequiredActiveContext();
        EntryDetailDto entity = workspaceEntryStore.getEntry(project, request.entryId());

        if (entity.approved() && !request.force()) {
            return entity;
        }

        EntryDetailDto corrected = applyCorrectionToEntry(entity);
        return workspaceEntryStore.saveEntry(project, corrected, SaveOptions.standard(project.localMember().principalId()));
    }

    public BatchCorrectionResultDto runCorrectionOnAll(BatchCorrectionRequestDto request) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        List<EntrySummaryDto> entities = workspaceEntryStore.listEntries(project);

        int corrected = 0;
        int skippedApproved = 0;

        for (EntrySummaryDto summary : entities) {
            EntryDetailDto detail = workspaceEntryStore.getEntry(project, summary.id());
            if (detail.approved() && !request.force()) {
                skippedApproved++;
                continue;
            }
            EntryDetailDto correctedEntry = applyCorrectionToEntry(detail);
            workspaceEntryStore.saveEntry(project, correctedEntry, SaveOptions.standard(project.localMember().principalId()));
            corrected++;
        }

        return new BatchCorrectionResultDto(entities.size(), corrected, skippedApproved);
    }

    public WorkspaceImportResultDto importEntries(WorkspaceImportRequestDto request) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        String rawText = defaultString(request.rawText()).trim();
        if (rawText.isBlank()) {
            throw new IllegalArgumentException("Import text is empty");
        }

        List<RawBlock> blocks = parseRawBlocks(rawText);
        List<EntrySummaryDto> existing = workspaceEntryStore.listEntries(project);
        int nextOrder = existing.stream().mapToInt(EntrySummaryDto::documentOrder).max().orElse(0) + 1;

        if (!(workspaceEntryStore instanceof JdbcWorkspaceEntryStore jdbcStore)) {
            throw new IllegalStateException("Current store does not support parsed import helper");
        }

        int created = 0;
        for (RawBlock block : blocks) {
            jdbcStore.insertParsedEntry(
                    project,
                    nextOrder++,
                    defaultString(block.chujText()),
                    defaultString(block.glossText()),
                    defaultString(block.translation()),
                    project.localMember().principalId()
            );
            created++;
        }

        int total = workspaceEntryStore.listEntries(project).size();
        return new WorkspaceImportResultDto(created, total);
    }

    public TextExportDto exportRawText(WorkspaceExportRequestDto request) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        List<EntrySummaryDto> entries = workspaceEntryStore.listEntries(project);

        StringBuilder sb = new StringBuilder();
        boolean wroteAny = false;
        for (EntrySummaryDto summary : entries) {
            EntryDetailDto entity = workspaceEntryStore.getEntry(project, summary.id());
            BlockText block = toExportBlock(entity, request.preferCorrected(), request.correctedOnly());
            if (block == null) continue;
            if (wroteAny) sb.append("\n");
            sb.append(block.chuj()).append("\n")
                    .append(block.gloss()).append("\n")
                    .append(block.translation()).append("\n");
            wroteAny = true;
        }
        return new TextExportDto("workspace.txt", sb.toString().trim());
    }

    public TextExportDto exportConllu(WorkspaceExportRequestDto request) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        List<EntrySummaryDto> entries = workspaceEntryStore.listEntries(project);

        StringBuilder sb = new StringBuilder();
        boolean wroteAny = false;
        for (EntrySummaryDto summary : entries) {
            EntryDetailDto entity = workspaceEntryStore.getEntry(project, summary.id());
            if (request.correctedOnly() && !hasUsableCorrectedText(entity)) continue;
            String preview = buildLiveConlluPreview(entity, request.preferCorrected());
            if (preview.isBlank()) continue;
            if (wroteAny) sb.append("\n");
            sb.append(preview.trim()).append("\n");
            wroteAny = true;
        }
        return new TextExportDto("workspace.conllu", sb.toString().trim());
    }

    private EntryDetailDto applyCorrectionToEntry(EntryDetailDto entity) {
        CorrectionEntry correctionEntry = processBlockText(
                defaultString(entity.rawChujText()),
                defaultString(entity.rawGlossText()),
                defaultString(entity.translation()),
                loadCorrectionRules()
        );

        return new EntryDetailDto(
                entity.id(),
                entity.documentOrder(),
                entity.rawChujText(),
                entity.rawGlossText(),
                entity.translation(),
                defaultString(correctionEntry.corrected().chujText()),
                defaultString(correctionEntry.corrected().glossText()),
                defaultString(correctionEntry.corrected().translation()),
                entity.approved(),
                new ConlluPipeline(annotationConfigComposerService.buildAnnotationConfig())
                        .toEntry(correctionEntry.corrected())
                        .toConlluString(),
                entity.version(),
                entity.updatedBy(),
                Instant.now()
        );
    }

    private String buildLiveConlluPreview(EntryDetailDto entity, boolean preferCorrected) {
        if (preferCorrected && hasUsableCorrectedText(entity)) {
            CorrectionEntry normalizedCorrected = processBlockText(
                    defaultString(entity.correctedChujText()),
                    defaultString(entity.correctedGlossText()),
                    defaultString(entity.correctedTranslation()),
                    List.of()
            );
            return new ConlluPipeline(annotationConfigComposerService.buildAnnotationConfig())
                    .toEntry(normalizedCorrected.corrected())
                    .toConlluString();
        }

        CorrectionEntry correctionEntry = processBlockText(
                defaultString(entity.rawChujText()),
                defaultString(entity.rawGlossText()),
                defaultString(entity.translation()),
                loadCorrectionRules()
        );
        return new ConlluPipeline(annotationConfigComposerService.buildAnnotationConfig())
                .toEntry(correctionEntry.corrected())
                .toConlluString();
    }

    private CorrectionEntry processBlockText(String chujText,
                                             String glossText,
                                             String translation,
                                             List<CorrectionRule> correctionRules) {
        try {
            ParityService service = new ParityService(
                    new RawTextReader(),
                    new RuleEngine(correctionRules),
                    annotationConfigComposerService.buildAnnotationConfig()
            );
            try (InputStream in = new ByteArrayInputStream(
                    (defaultString(chujText) + "\n" + defaultString(glossText) + "\n" + defaultString(translation) + "\n")
                            .getBytes(StandardCharsets.UTF_8))) {
                List<CorrectionEntry> out = service.correct(in);
                if (out.isEmpty()) {
                    throw new IllegalStateException("Correction engine returned no entries");
                }
                return out.getFirst();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot process block: " + e.getMessage(), e);
        }
    }

    private List<CorrectionRule> loadCorrectionRules() {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        String yaml = ruleStore.listRulesByKind(project, RuleKind.CORRECTION).stream()
                .filter(RuleDetailDto::enabled)
                .map(RuleDetailDto::rawYaml)
                .filter(raw -> raw != null && !raw.isBlank())
                .reduce("", (left, right) -> left + "\n" + right)
                .trim();

        if (yaml.isBlank()) return List.of();

        return new YamlRuleLoader().load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    private List<RawBlock> parseRawBlocks(String rawText) {
        try (InputStream in = new ByteArrayInputStream(rawText.getBytes(StandardCharsets.UTF_8))) {
            return new RawTextReader().read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot import entries: " + e.getMessage(), e);
        }
    }

    private BlockText toExportBlock(EntryDetailDto entity, boolean preferCorrected, boolean correctedOnly) {
        boolean useCorrected = preferCorrected && hasUsableCorrectedText(entity);
        if (correctedOnly && !useCorrected) return null;

        String chuj = useCorrected ? defaultString(entity.correctedChujText()) : defaultString(entity.rawChujText());
        String gloss = useCorrected ? defaultString(entity.correctedGlossText()) : defaultString(entity.rawGlossText());
        String translation = useCorrected ? defaultString(entity.correctedTranslation()) : defaultString(entity.translation());
        return new BlockText(chuj, gloss, translation);
    }

    private boolean hasUsableCorrectedText(EntryDetailDto entity) {
        return !defaultString(entity.correctedChujText()).isBlank()
                || !defaultString(entity.correctedGlossText()).isBlank()
                || !defaultString(entity.correctedTranslation()).isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private record BlockText(String chuj, String gloss, String translation) {
    }
}