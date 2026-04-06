package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.model.RuleEntity;
import org.titiplex.backend.model.WorkspaceEntryEntity;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;
import org.titiplex.conllu.AnnotationConfig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceEntryService {

    private final WorkspaceEntryRepository workspaceEntryRepository;
    private final RuleRepository ruleRepository;
    private final AnnotationConfigComposerService annotationConfigComposerService;

    public WorkspaceEntryService(WorkspaceEntryRepository workspaceEntryRepository,
                                 RuleRepository ruleRepository,
                                 AnnotationConfigComposerService annotationConfigComposerService) {
        this.workspaceEntryRepository = workspaceEntryRepository;
        this.ruleRepository = ruleRepository;
        this.annotationConfigComposerService = annotationConfigComposerService;
    }

    public List<EntrySummaryDto> listEntries() {
        return workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc().stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public EntryDetailDto getEntry(UUID id) {
        WorkspaceEntryEntity entity = workspaceEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));
        return toDetailDto(entity, true);
    }

    public EntryDetailDto saveEntry(EntryDetailDto dto) {
        UUID id = dto.id() != null ? dto.id() : UUID.randomUUID();

        WorkspaceEntryEntity entity = workspaceEntryRepository.findById(id)
                .orElseGet(WorkspaceEntryEntity::new);

        entity.setId(id);
        entity.setDocumentOrder(dto.documentOrder());
        entity.setRawChujText(defaultString(dto.rawChujText()));
        entity.setRawGlossText(defaultString(dto.rawGlossText()));
        entity.setTranslation(defaultString(dto.translation()));
        entity.setCorrectedChujText(defaultString(dto.correctedChujText()));
        entity.setCorrectedGlossText(defaultString(dto.correctedGlossText()));
        entity.setCorrectedTranslation(defaultString(dto.correctedTranslation()));
        entity.setApproved(dto.approved());
        entity.setConlluPreview(defaultString(dto.conlluPreview()));

        WorkspaceEntryEntity saved = workspaceEntryRepository.save(entity);
        return toDetailDto(saved, true);
    }

    public EntryDetailDto runCorrection(CorrectionRunRequestDto request) {
        if (request.entryId() == null) {
            throw new IllegalArgumentException("entryId cannot be null");
        }

        WorkspaceEntryEntity entity = workspaceEntryRepository.findById(request.entryId())
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + request.entryId()));

        if (entity.isApproved() && !request.force()) {
            return toDetailDto(entity, true);
        }

        applyCorrectionToEntity(entity);
        WorkspaceEntryEntity saved = workspaceEntryRepository.save(entity);
        return toDetailDto(saved, true);
    }

    public BatchCorrectionResultDto runCorrectionOnAll(BatchCorrectionRequestDto request) {
        List<WorkspaceEntryEntity> entities = workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc();

        int corrected = 0;
        int skippedApproved = 0;

        for (WorkspaceEntryEntity entity : entities) {
            if (entity.isApproved() && !request.force()) {
                skippedApproved++;
                continue;
            }

            applyCorrectionToEntity(entity);
            corrected++;
        }

        workspaceEntryRepository.saveAll(entities);

        return new BatchCorrectionResultDto(
                entities.size(),
                corrected,
                skippedApproved
        );
    }

    public WorkspaceImportResultDto importEntries(WorkspaceImportRequestDto request) {
        String rawText = defaultString(request.rawText()).trim();
        if (rawText.isBlank()) {
            throw new IllegalArgumentException("Import text is empty");
        }

        List<RawBlock> blocks = parseRawBlocks(rawText);

        if (request.replaceExisting()) {
            workspaceEntryRepository.deleteAll();
        }

        int nextOrder = workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc().stream()
                .mapToInt(WorkspaceEntryEntity::getDocumentOrder)
                .max()
                .orElse(0) + 1;

        List<WorkspaceEntryEntity> created = new ArrayList<>();
        for (RawBlock block : blocks) {
            created.add(new WorkspaceEntryEntity(
                    UUID.randomUUID(),
                    nextOrder++,
                    defaultString(block.chujText()),
                    defaultString(block.glossText()),
                    defaultString(block.translation()),
                    "",
                    "",
                    "",
                    false,
                    ""
            ));
        }

        workspaceEntryRepository.saveAll(created);

        return new WorkspaceImportResultDto(
                created.size(),
                (int) workspaceEntryRepository.count()
        );
    }

    public TextExportDto exportRawText(WorkspaceExportRequestDto request) {
        List<WorkspaceEntryEntity> entities = workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc();

        StringBuilder sb = new StringBuilder();
        boolean wroteAny = false;

        for (WorkspaceEntryEntity entity : entities) {
            BlockText block = toExportBlock(entity, request.preferCorrected(), request.correctedOnly());
            if (block == null) {
                continue;
            }

            if (wroteAny) {
                sb.append("\n");
            }

            sb.append(block.chuj()).append("\n")
                    .append(block.gloss()).append("\n")
                    .append(block.translation()).append("\n");

            wroteAny = true;
        }

        return new TextExportDto("workspace.txt", sb.toString().trim());
    }

    public TextExportDto exportConllu(WorkspaceExportRequestDto request) {
        List<WorkspaceEntryEntity> entities = workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc();

        StringBuilder sb = new StringBuilder();
        boolean wroteAny = false;

        for (WorkspaceEntryEntity entity : entities) {
            if (request.correctedOnly() && !hasUsableCorrectedText(entity)) {
                continue;
            }

            String preview = buildLiveConlluPreview(entity, request.preferCorrected());
            if (preview.isBlank()) {
                continue;
            }

            if (wroteAny) {
                sb.append("\n");
            }

            sb.append(preview.trim()).append("\n");
            wroteAny = true;
        }

        return new TextExportDto("workspace.conllu", sb.toString().trim());
    }

    public void seedDemoEntriesIfEmpty() {
        if (workspaceEntryRepository.count() > 0) {
            return;
        }

        workspaceEntryRepository.save(new WorkspaceEntryEntity(
                UUID.randomUUID(),
                1,
                "Ix-naq aj winh",
                "A1-B2 ganar DET hombre",
                "L’homme nous a gagnés.",
                "",
                "",
                "",
                false,
                ""
        ));

        workspaceEntryRepository.save(new WorkspaceEntryEntity(
                UUID.randomUUID(),
                2,
                "Ha’ ix to",
                "DEM A1 ir",
                "Celui-ci va.",
                "",
                "",
                "",
                false,
                ""
        ));
    }

    private void applyCorrectionToEntity(WorkspaceEntryEntity entity) {
        CorrectionEntry correctionEntry = correctRawEntity(entity);
        AnnotationConfig annotationConfig = annotationConfigComposerService.buildAnnotationConfig();

        entity.setCorrectedChujText(defaultString(correctionEntry.corrected().chujText()));
        entity.setCorrectedGlossText(defaultString(correctionEntry.corrected().glossText()));
        entity.setCorrectedTranslation(defaultString(correctionEntry.corrected().translation()));
        entity.setConlluPreview(new ConlluPipeline(annotationConfig)
                .toEntry(correctionEntry.corrected())
                .toConlluString());
    }

    private String buildLiveConlluPreview(WorkspaceEntryEntity entity, boolean preferCorrected) {
        AnnotationConfig annotationConfig = annotationConfigComposerService.buildAnnotationConfig();

        if (preferCorrected && hasUsableCorrectedText(entity)) {
            CorrectionEntry normalizedCorrected = normalizeAlreadyCorrectedText(entity);
            return new ConlluPipeline(annotationConfig)
                    .toEntry(normalizedCorrected.corrected())
                    .toConlluString();
        }

        CorrectionEntry correctionEntry = correctRawEntity(entity);
        return new ConlluPipeline(annotationConfig)
                .toEntry(correctionEntry.corrected())
                .toConlluString();
    }

    private CorrectionEntry normalizeAlreadyCorrectedText(WorkspaceEntryEntity entity) {
        return processBlockText(
                defaultString(entity.getCorrectedChujText()),
                defaultString(entity.getCorrectedGlossText()),
                defaultString(entity.getCorrectedTranslation()),
                List.of()
        );
    }

    private CorrectionEntry correctRawEntity(WorkspaceEntryEntity entity) {
        return processBlockText(
                defaultString(entity.getRawChujText()),
                defaultString(entity.getRawGlossText()),
                defaultString(entity.getTranslation()),
                loadCorrectionRules()
        );
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
                    (defaultString(chujText) + "\n"
                            + defaultString(glossText) + "\n"
                            + defaultString(translation) + "\n")
                            .getBytes(StandardCharsets.UTF_8)
            )) {
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
        String yaml = ruleRepository.findByKindOrderByPriorityAscNameAsc(RuleKind.CORRECTION).stream()
                .filter(RuleEntity::isEnabled)
                .map(RuleEntity::getRawYaml)
                .filter(raw -> raw != null && !raw.isBlank())
                .reduce("", (left, right) -> left + "\n" + right)
                .trim();

        if (yaml.isBlank()) {
            return List.of();
        }

        return new YamlRuleLoader().load(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))
        );
    }

    private List<RawBlock> parseRawBlocks(String rawText) {
        try (InputStream in = new ByteArrayInputStream(rawText.getBytes(StandardCharsets.UTF_8))) {
            return new RawTextReader().read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot import entries: " + e.getMessage(), e);
        }
    }

    private BlockText toExportBlock(WorkspaceEntryEntity entity,
                                    boolean preferCorrected,
                                    boolean correctedOnly) {
        boolean useCorrected = preferCorrected && hasUsableCorrectedText(entity);

        if (correctedOnly && !useCorrected) {
            return null;
        }

        String chuj = useCorrected
                ? defaultString(entity.getCorrectedChujText())
                : defaultString(entity.getRawChujText());
        String gloss = useCorrected
                ? defaultString(entity.getCorrectedGlossText())
                : defaultString(entity.getRawGlossText());
        String translation = useCorrected
                ? defaultString(entity.getCorrectedTranslation())
                : defaultString(entity.getTranslation());

        return new BlockText(chuj, gloss, translation);
    }

    private EntrySummaryDto toSummaryDto(WorkspaceEntryEntity entity) {
        return new EntrySummaryDto(
                entity.getId(),
                entity.getDocumentOrder(),
                defaultString(entity.getRawChujText()),
                defaultString(entity.getRawGlossText()),
                defaultString(entity.getTranslation()),
                entity.isApproved(),
                hasUsableCorrectedText(entity)
        );
    }

    private EntryDetailDto toDetailDto(WorkspaceEntryEntity entity, boolean livePreview) {
        String preview = livePreview
                ? buildLiveConlluPreview(entity, true)
                : defaultString(entity.getConlluPreview());

        return new EntryDetailDto(
                entity.getId(),
                entity.getDocumentOrder(),
                defaultString(entity.getRawChujText()),
                defaultString(entity.getRawGlossText()),
                defaultString(entity.getTranslation()),
                defaultString(entity.getCorrectedChujText()),
                defaultString(entity.getCorrectedGlossText()),
                defaultString(entity.getCorrectedTranslation()),
                entity.isApproved(),
                defaultString(preview)
        );
    }

    private boolean hasUsableCorrectedText(WorkspaceEntryEntity entity) {
        return !defaultString(entity.getCorrectedChujText()).isBlank()
                || !defaultString(entity.getCorrectedGlossText()).isBlank()
                || !defaultString(entity.getCorrectedTranslation()).isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private record BlockText(String chuj, String gloss, String translation) {
    }
}