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
import org.titiplex.model.CorrectedBlock;
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

    public WorkspaceEntryService(WorkspaceEntryRepository workspaceEntryRepository,
                                 RuleRepository ruleRepository) {
        this.workspaceEntryRepository = workspaceEntryRepository;
        this.ruleRepository = ruleRepository;
    }

    public List<EntrySummaryDto> listEntries() {
        return workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc().stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public EntryDetailDto getEntry(UUID id) {
        WorkspaceEntryEntity entity = workspaceEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + id));
        return toDetailDto(entity);
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
        return toDetailDto(saved);
    }

    public EntryDetailDto runCorrection(CorrectionRunRequestDto request) {
        if (request.entryId() == null) {
            throw new IllegalArgumentException("entryId cannot be null");
        }

        WorkspaceEntryEntity entity = workspaceEntryRepository.findById(request.entryId())
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + request.entryId()));

        if (entity.isApproved() && !request.force()) {
            return toDetailDto(entity);
        }

        applyCorrectionToEntity(entity);
        WorkspaceEntryEntity saved = workspaceEntryRepository.save(entity);
        return toDetailDto(saved);
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
            if (request.correctedOnly() && !hasCorrection(entity)) {
                continue;
            }

            String preview = ensureConlluPreview(entity, request.preferCorrected());
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
        CorrectionEntry correctionEntry = correctEntity(entity);

        entity.setCorrectedChujText(defaultString(correctionEntry.corrected().chujText()));
        entity.setCorrectedGlossText(defaultString(correctionEntry.corrected().glossText()));
        entity.setCorrectedTranslation(defaultString(correctionEntry.corrected().translation()));
        entity.setConlluPreview(new ConlluPipeline(new AnnotationConfig())
                .toEntry(correctionEntry.corrected())
                .toConlluString());
    }

    private String ensureConlluPreview(WorkspaceEntryEntity entity, boolean preferCorrected) {
        if (!defaultString(entity.getConlluPreview()).isBlank()) {
            return entity.getConlluPreview();
        }

        if (preferCorrected && hasCorrection(entity)) {
            CorrectedBlock block = new CorrectedBlock(
                    entity.getDocumentOrder(),
                    defaultString(entity.getCorrectedChujText()),
                    defaultString(entity.getCorrectedGlossText()),
                    defaultString(entity.getCorrectedTranslation()),
                    List.of()
            );
            return new ConlluPipeline(new AnnotationConfig()).toEntry(block).toConlluString();
        }

        CorrectionEntry correctionEntry = correctEntity(entity);
        return new ConlluPipeline(new AnnotationConfig())
                .toEntry(correctionEntry.corrected())
                .toConlluString();
    }

    private CorrectionEntry correctEntity(WorkspaceEntryEntity entity) {
        try {
            List<CorrectionRule> rules = loadCorrectionRules();

            ParityService service = new ParityService(
                    new RawTextReader(),
                    new RuleEngine(rules),
                    new AnnotationConfig()
            );

            try (InputStream in = new ByteArrayInputStream(buildRawBlock(entity).getBytes(StandardCharsets.UTF_8))) {
                List<CorrectionEntry> out = service.correct(in);
                if (out.isEmpty()) {
                    throw new IllegalStateException("Correction engine returned no entries");
                }
                return out.getFirst();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot run correction: " + e.getMessage(), e);
        }
    }

    private List<CorrectionRule> loadCorrectionRules() {
        String yaml = ruleRepository.findByKindOrderByPriorityAscNameAsc(RuleKind.CORRECTION).stream()
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

    private String buildRawBlock(WorkspaceEntryEntity entity) {
        return defaultString(entity.getRawChujText()) + "\n"
                + defaultString(entity.getRawGlossText()) + "\n"
                + defaultString(entity.getTranslation()) + "\n";
    }

    private BlockText toExportBlock(WorkspaceEntryEntity entity,
                                    boolean preferCorrected,
                                    boolean correctedOnly) {
        boolean useCorrected = preferCorrected && hasCorrection(entity);

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
                hasCorrection(entity)
        );
    }

    private EntryDetailDto toDetailDto(WorkspaceEntryEntity entity) {
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
                defaultString(entity.getConlluPreview())
        );
    }

    private boolean hasCorrection(WorkspaceEntryEntity entity) {
        return !defaultString(entity.getCorrectedChujText()).isBlank()
                || !defaultString(entity.getCorrectedGlossText()).isBlank()
                || !defaultString(entity.getConlluPreview()).isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private record BlockText(String chuj, String gloss, String translation) {
    }
}