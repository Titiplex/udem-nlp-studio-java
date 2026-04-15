package org.titiplex.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.titiplex.backend.domain.rule.RuleDefinition;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.mapper.RuleMapper;
import org.titiplex.backend.model.RuleEntity;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceExchangeService {

    private final WorkspaceEntryRepository workspaceEntryRepository;
    private final RuleRepository ruleRepository;
    private final WorkspaceEntryService workspaceEntryService;
    private final RuleService ruleService;
    private final RuleMapper ruleMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WorkspaceExchangeService(WorkspaceEntryRepository workspaceEntryRepository,
                                    RuleRepository ruleRepository,
                                    WorkspaceEntryService workspaceEntryService,
                                    RuleService ruleService,
                                    RuleMapper ruleMapper) {
        this.workspaceEntryRepository = workspaceEntryRepository;
        this.ruleRepository = ruleRepository;
        this.workspaceEntryService = workspaceEntryService;
        this.ruleService = ruleService;
        this.ruleMapper = ruleMapper;
    }

    public TextExportDto exportData(WorkspaceExchangeRequestDto request) {
        String format = normalize(request.format());

        return switch (format) {
            case "raw_text" -> workspaceEntryService.exportRawText(
                    new WorkspaceExportRequestDto(request.preferCorrected(), request.correctedOnly())
            );
            case "conllu" -> workspaceEntryService.exportConllu(
                    new WorkspaceExportRequestDto(request.preferCorrected(), request.correctedOnly())
            );
            case "entries_json" -> new TextExportDto(
                    "workspace-entries.json",
                    writePretty(listDetailedEntries())
            );
            case "entries_csv" -> new TextExportDto(
                    "workspace-entries.csv",
                    buildEntriesCsv(listDetailedEntries())
            );
            case "entries_sql" -> new TextExportDto(
                    "workspace-entries.sql",
                    buildEntriesSql(listDetailedEntries())
            );
            case "rules_json" -> new TextExportDto(
                    "workspace-rules.json",
                    writePretty(listDetailedRules(request.ruleKinds(), request.onlyEnabledRules()))
            );
            case "rules_yaml" -> new TextExportDto(
                    "workspace-rules.yaml",
                    buildRulesYaml(listDetailedRules(request.ruleKinds(), request.onlyEnabledRules()))
            );
            case "workspace_bundle_json" -> new TextExportDto(
                    "workspace-bundle.json",
                    writePretty(new WorkspaceDataBundleDto(
                            listDetailedEntries(),
                            listDetailedRules(request.ruleKinds(), request.onlyEnabledRules())
                    ))
            );
            default -> throw new IllegalArgumentException("Unsupported export format: " + request.format());
        };
    }

    public WorkspaceDataImportResultDto importData(String format,
                                                   String content,
                                                   boolean replaceExistingEntries,
                                                   boolean replaceExistingRules) {
        String normalizedFormat = normalize(format);
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Import content is empty");
        }

        return switch (normalizedFormat) {
            case "raw_text" -> importRawText(trimmed, replaceExistingEntries);
            case "entries_json" -> importEntriesJson(trimmed, replaceExistingEntries);
            case "rules_json" -> importRulesJson(trimmed, replaceExistingRules);
            case "workspace_bundle_json" -> importBundleJson(trimmed, replaceExistingEntries, replaceExistingRules);
            default -> throw new IllegalArgumentException("Unsupported import format: " + format);
        };
    }

    private WorkspaceDataImportResultDto importRawText(String content, boolean replaceExistingEntries) {
        WorkspaceImportResultDto result = workspaceEntryService.importEntries(
                new WorkspaceImportRequestDto(content, replaceExistingEntries)
        );

        return new WorkspaceDataImportResultDto(
                result.importedEntries(),
                0,
                result.importedEntries() + " entries imported from raw text."
        );
    }

    private WorkspaceDataImportResultDto importEntriesJson(String content, boolean replaceExistingEntries) {
        try {
            List<EntryDetailDto> entries = objectMapper.readValue(content, new TypeReference<>() {
            });

            if (replaceExistingEntries) {
                workspaceEntryRepository.deleteAll();
            }

            int imported = 0;
            for (EntryDetailDto entry : entries) {
                workspaceEntryService.saveEntry(new EntryDetailDto(
                        null,
                        entry.documentOrder(),
                        defaultString(entry.contextText()),
                        defaultString(entry.surfaceText()),
                        defaultString(entry.rawChujText()),
                        defaultString(entry.rawGlossText()),
                        defaultString(entry.translation()),
                        defaultString(entry.comments()),
                        defaultString(entry.correctedChujText()),
                        defaultString(entry.correctedGlossText()),
                        defaultString(entry.correctedTranslation()),
                        entry.approved(),
                        defaultString(entry.conlluPreview())
                ));
                imported++;
            }

            return new WorkspaceDataImportResultDto(
                    imported,
                    0,
                    imported + " entries imported from JSON."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot import entries JSON: " + e.getMessage(), e);
        }
    }

    private WorkspaceDataImportResultDto importRulesJson(String content, boolean replaceExistingRules) {
        try {
            List<RuleDetailDto> rules = objectMapper.readValue(content, new TypeReference<>() {
            });

            if (replaceExistingRules) {
                ruleRepository.deleteAll();
            }

            int imported = 0;
            for (RuleDetailDto rule : rules) {
                ruleService.saveRule(new RuleDetailDto(
                        null,
                        defaultString(rule.name()),
                        rule.kind(),
                        defaultString(rule.subtype()),
                        defaultString(rule.scope()),
                        rule.enabled(),
                        rule.priority(),
                        defaultString(rule.description()),
                        rule.payload(),
                        defaultString(rule.rawYaml())
                ));
                imported++;
            }

            return new WorkspaceDataImportResultDto(
                    0,
                    imported,
                    imported + " rules imported from JSON."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot import rules JSON: " + e.getMessage(), e);
        }
    }

    private WorkspaceDataImportResultDto importBundleJson(String content,
                                                          boolean replaceExistingEntries,
                                                          boolean replaceExistingRules) {
        try {
            WorkspaceDataBundleDto bundle = objectMapper.readValue(content, WorkspaceDataBundleDto.class);

            int importedEntries = 0;
            int importedRules = 0;

            if (replaceExistingEntries) {
                workspaceEntryRepository.deleteAll();
            }
            if (replaceExistingRules) {
                ruleRepository.deleteAll();
            }

            for (EntryDetailDto entry : safeList(bundle.entries())) {
                workspaceEntryService.saveEntry(new EntryDetailDto(
                        null,
                        entry.documentOrder(),
                        defaultString(entry.contextText()),
                        defaultString(entry.surfaceText()),
                        defaultString(entry.rawChujText()),
                        defaultString(entry.rawGlossText()),
                        defaultString(entry.translation()),
                        defaultString(entry.comments()),
                        defaultString(entry.correctedChujText()),
                        defaultString(entry.correctedGlossText()),
                        defaultString(entry.correctedTranslation()),
                        entry.approved(),
                        defaultString(entry.conlluPreview())
                ));
                importedEntries++;
            }

            for (RuleDetailDto rule : safeList(bundle.rules())) {
                ruleService.saveRule(new RuleDetailDto(
                        null,
                        defaultString(rule.name()),
                        rule.kind(),
                        defaultString(rule.subtype()),
                        defaultString(rule.scope()),
                        rule.enabled(),
                        rule.priority(),
                        defaultString(rule.description()),
                        rule.payload(),
                        defaultString(rule.rawYaml())
                ));
                importedRules++;
            }

            return new WorkspaceDataImportResultDto(
                    importedEntries,
                    importedRules,
                    importedEntries + " entries and " + importedRules + " rules imported from bundle."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot import bundle JSON: " + e.getMessage(), e);
        }
    }

    private List<EntryDetailDto> listDetailedEntries() {
        return workspaceEntryRepository.findAllByOrderByDocumentOrderAscIdAsc().stream()
                .map(entity -> workspaceEntryService.getEntry(entity.getId()))
                .toList();
    }

    private List<RuleDetailDto> listDetailedRules(List<String> ruleKinds, boolean onlyEnabledRules) {
        List<String> normalizedKinds = safeList(ruleKinds).stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .toList();

        return ruleRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(RuleEntity::getPriority)
                        .thenComparing(RuleEntity::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .filter(rule -> !onlyEnabledRules || rule.isEnabled())
                .filter(rule -> normalizedKinds.isEmpty() || normalizedKinds.contains(rule.getKind().name()))
                .map(ruleMapper::toDomain)
                .map(this::toDetailDto)
                .toList();
    }

    private RuleDetailDto toDetailDto(RuleDefinition definition) {
        return new RuleDetailDto(
                definition.id(),
                definition.name(),
                definition.kind(),
                definition.subtype(),
                definition.scope(),
                definition.enabled(),
                definition.priority(),
                definition.description(),
                definition.payload(),
                definition.rawYaml()
        );
    }

    private String buildRulesYaml(List<RuleDetailDto> rules) {
        return rules.stream()
                .map(rule -> defaultString(rule.rawYaml()).trim())
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining("\n\n"));
    }

    private String buildEntriesCsv(List<EntryDetailDto> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,documentOrder,rawChujText,rawGlossText,translation,correctedChujText,correctedGlossText,correctedTranslation,approved,conlluPreview\n");

        for (EntryDetailDto entry : entries) {
            sb.append(csv(entry.id() == null ? "" : entry.id().toString())).append(',')
                    .append(entry.documentOrder()).append(',')
                    .append(csv(entry.rawChujText())).append(',')
                    .append(csv(entry.rawGlossText())).append(',')
                    .append(csv(entry.translation())).append(',')
                    .append(csv(entry.correctedChujText())).append(',')
                    .append(csv(entry.correctedGlossText())).append(',')
                    .append(csv(entry.correctedTranslation())).append(',')
                    .append(entry.approved()).append(',')
                    .append(csv(entry.conlluPreview()))
                    .append('\n');
        }

        return sb.toString().trim();
    }

    private String buildEntriesSql(List<EntryDetailDto> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- Generated by NLP Studio\n");
        sb.append("-- Import manually into table workspace_entries\n\n");

        for (EntryDetailDto entry : entries) {
            String id = entry.id() == null ? UUID.randomUUID().toString() : entry.id().toString();

            sb.append("INSERT INTO workspace_entries ")
                    .append("(id, document_order, raw_chuj_text, raw_gloss_text, translation, corrected_chuj_text, corrected_gloss_text, corrected_translation, approved, conllu_preview) VALUES (")
                    .append(sql(id)).append(", ")
                    .append(entry.documentOrder()).append(", ")
                    .append(sql(entry.rawChujText())).append(", ")
                    .append(sql(entry.rawGlossText())).append(", ")
                    .append(sql(entry.translation())).append(", ")
                    .append(sql(entry.correctedChujText())).append(", ")
                    .append(sql(entry.correctedGlossText())).append(", ")
                    .append(sql(entry.correctedTranslation())).append(", ")
                    .append(entry.approved() ? "TRUE" : "FALSE").append(", ")
                    .append(sql(entry.conlluPreview())).append(");\n");
        }

        return sb.toString().trim();
    }

    private String csv(String value) {
        String safe = defaultString(value).replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private String sql(String value) {
        return "'" + defaultString(value).replace("'", "''") + "'";
    }

    private String writePretty(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize export payload", e);
        }
    }

    private String normalize(String format) {
        return format == null ? "" : format.trim().toLowerCase(Locale.ROOT);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}