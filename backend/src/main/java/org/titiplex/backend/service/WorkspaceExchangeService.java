package org.titiplex.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.titiplex.backend.domain.rule.RuleDefinition;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.mapper.RuleMapper;
import org.titiplex.backend.model.RuleEntity;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

@Service
public class WorkspaceExchangeService {

    private final WorkspaceEntryRepository workspaceEntryRepository;
    private final RuleRepository ruleRepository;
    private final WorkspaceEntryService workspaceEntryService;
    private final RuleService ruleService;
    private final RuleMapper ruleMapper;
    private final AnnotationSettingsService annotationSettingsService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Yaml yamlReader;
    private final Yaml yamlWriter;

    public WorkspaceExchangeService(WorkspaceEntryRepository workspaceEntryRepository,
                                    RuleRepository ruleRepository,
                                    WorkspaceEntryService workspaceEntryService,
                                    RuleService ruleService,
                                    RuleMapper ruleMapper,
                                    AnnotationSettingsService annotationSettingsService) {
        this.workspaceEntryRepository = workspaceEntryRepository;
        this.ruleRepository = ruleRepository;
        this.workspaceEntryService = workspaceEntryService;
        this.ruleService = ruleService;
        this.ruleMapper = ruleMapper;
        this.annotationSettingsService = annotationSettingsService;

        this.yamlReader = new Yaml();

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setIndicatorIndent(1);
        options.setWidth(160);

        this.yamlWriter = new Yaml(options);
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
            case "correction_rules_yaml" -> new TextExportDto(
                    "workspace-correction-rules.yaml",
                    buildRulesYaml(listDetailedRules(List.of("CORRECTION"), request.onlyEnabledRules()))
            );
            case "annotation_rules_yaml" -> new TextExportDto(
                    "workspace-annotation-rules.yaml",
                    buildRulesYaml(listDetailedRules(List.of("ANNOTATION"), request.onlyEnabledRules()))
            );
            case "annotation_settings_json" -> new TextExportDto(
                    "workspace-annotation-settings.json",
                    writePretty(annotationSettingsService.getSettings())
            );
            case "annotation_settings_yaml" -> new TextExportDto(
                    "workspace-annotation-settings.yaml",
                    buildAnnotationSettingsYaml(annotationSettingsService.getSettings())
            );
            case "workspace_bundle_json" -> new TextExportDto(
                    "workspace-bundle.json",
                    writePretty(new WorkspaceDataBundleDto(
                            listDetailedEntries(),
                            listDetailedRules(request.ruleKinds(), request.onlyEnabledRules()),
                            request.includeAnnotationSettings() ? annotationSettingsService.getSettings() : null
                    ))
            );
            default -> throw new IllegalArgumentException("Unsupported export format: " + request.format());
        };
    }

    public WorkspaceDataImportResultDto importData(String format,
                                                   String content,
                                                   boolean replaceExistingEntries,
                                                   boolean replaceExistingRules,
                                                   boolean replaceAnnotationSettings) {
        String normalizedFormat = normalize(format);
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Import content is empty");
        }

        return switch (normalizedFormat) {
            case "raw_text" -> importRawText(trimmed, replaceExistingEntries);
            case "entries_json" -> importEntriesJson(trimmed, replaceExistingEntries);
            case "rules_json" -> importRulesJson(trimmed, replaceExistingRules);
            case "correction_rules_yaml" ->
                    importRulesYaml(trimmed, replaceExistingRules, RuleKind.CORRECTION, "yaml_correction");
            case "annotation_rules_yaml" ->
                    importRulesYaml(trimmed, replaceExistingRules, RuleKind.ANNOTATION, "yaml_annotation");
            case "annotation_settings_json" -> importAnnotationSettingsJson(trimmed, replaceAnnotationSettings);
            case "annotation_settings_yaml" -> importAnnotationSettingsYaml(trimmed, replaceAnnotationSettings);
            case "workspace_bundle_json" ->
                    importBundleJson(trimmed, replaceExistingEntries, replaceExistingRules, replaceAnnotationSettings);
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

    private WorkspaceDataImportResultDto importRulesYaml(String content,
                                                         boolean replaceExistingRules,
                                                         RuleKind kind,
                                                         String defaultSubtype) {
        try {
            List<Map<String, Object>> rules = extractRulePayloads(content, Map.of());
            if (rules.isEmpty()) {
                throw new IllegalArgumentException("YAML rules import expects at least one rule.");
            }

            if (replaceExistingRules) {
                ruleRepository.deleteAll();
            }

            int imported = 0;
            for (Map<String, Object> payload : rules) {
                String name = String.valueOf(payload.getOrDefault("name", kind.name() + " imported rule"));
                String scope = String.valueOf(payload.getOrDefault("scope", "token"));
                String description = payload.get("description") == null ? "" : String.valueOf(payload.get("description"));
                String rawYaml = yamlWriter.dump(List.of(payload));

                ruleService.saveRule(new RuleDetailDto(
                        null,
                        name,
                        kind,
                        defaultSubtype,
                        scope,
                        true,
                        100,
                        description,
                        payload,
                        rawYaml
                ));
                imported++;
            }

            return new WorkspaceDataImportResultDto(
                    0,
                    imported,
                    imported + " " + kind.name().toLowerCase(Locale.ROOT) + " rules imported from YAML."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot import rules YAML: " + e.getMessage(), e);
        }
    }

    private WorkspaceDataImportResultDto importAnnotationSettingsJson(String content,
                                                                      boolean replaceAnnotationSettings) {
        try {
            AnnotationSettingsDto dto = objectMapper.readValue(content, AnnotationSettingsDto.class);
            AnnotationSettingsDto source = replaceAnnotationSettings
                    ? dto
                    : mergeAnnotationSettings(annotationSettingsService.getSettings(), dto);

            annotationSettingsService.saveSettings(source);

            return new WorkspaceDataImportResultDto(
                    0,
                    0,
                    "Annotation settings imported from JSON."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot import annotation settings JSON: " + e.getMessage(), e);
        }
    }

    private WorkspaceDataImportResultDto importAnnotationSettingsYaml(String content,
                                                                      boolean replaceAnnotationSettings) {
        try {
            Object loaded = yamlReader.load(content);
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Annotation settings YAML must be an object.");
            }

            AnnotationSettingsDto dto = mapToAnnotationSettingsDto(deepStringKeyMap(map));
            AnnotationSettingsDto source = replaceAnnotationSettings
                    ? dto
                    : mergeAnnotationSettings(annotationSettingsService.getSettings(), dto);

            annotationSettingsService.saveSettings(source);

            return new WorkspaceDataImportResultDto(
                    0,
                    0,
                    "Annotation settings imported from YAML."
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot import annotation settings YAML: " + e.getMessage(), e);
        }
    }

    private WorkspaceDataImportResultDto importBundleJson(String content,
                                                          boolean replaceExistingEntries,
                                                          boolean replaceExistingRules,
                                                          boolean replaceAnnotationSettings) {
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

            if (bundle.annotationSettings() != null) {
                AnnotationSettingsDto merged = replaceAnnotationSettings
                        ? bundle.annotationSettings()
                        : mergeAnnotationSettings(annotationSettingsService.getSettings(), bundle.annotationSettings());
                annotationSettingsService.saveSettings(merged);
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
        List<Map<String, Object>> mergedRules = new ArrayList<>();

        for (RuleDetailDto rule : rules) {
            try {
                mergedRules.addAll(extractRulePayloads(
                        defaultString(rule.rawYaml()),
                        rule.payload() == null ? Map.of() : rule.payload()
                ));
            } catch (Exception e) {
                Map<String, Object> fallbackPayload = rule.payload() == null ? Map.of() : deepStringKeyMap(rule.payload());
                if (!fallbackPayload.isEmpty()) {
                    mergedRules.add(fallbackPayload);
                } else {
                    throw new IllegalStateException(
                            "Cannot export YAML for rule '" + defaultString(rule.name()) + "': " + e.getMessage(),
                            e
                    );
                }
            }
        }

        return mergedRules.isEmpty() ? "" : yamlWriter.dump(mergedRules);
    }

    private List<Map<String, Object>> extractRulePayloads(String rawYaml, Map<String, Object> fallbackPayload) {
        String normalized = defaultString(rawYaml).trim();
        if (!normalized.isBlank()) {
            Object loaded = yamlReader.load(normalized);
            List<Map<String, Object>> extracted = normalizeLoadedRuleYaml(loaded);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }

        if (fallbackPayload != null && !fallbackPayload.isEmpty()) {
            return List.of(deepStringKeyMap(fallbackPayload));
        }

        return List.of();
    }

    private List<Map<String, Object>> normalizeLoadedRuleYaml(Object loaded) {
        List<Map<String, Object>> out = new ArrayList<>();

        if (loaded == null) {
            return out;
        }

        if (loaded instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    out.add(deepStringKeyMap(map));
                }
            }
            return out;
        }

        if (loaded instanceof Map<?, ?> map) {
            Map<String, Object> normalized = deepStringKeyMap(map);
            Object rulesObj = normalized.get("rules");

            if (rulesObj instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> itemMap) {
                        out.add(deepStringKeyMap(itemMap));
                    }
                }
                return out;
            }

            if (looksLikeRuleDefinition(normalized)) {
                out.add(normalized);
            }
        }

        return out;
    }

    private boolean looksLikeRuleDefinition(Map<String, Object> map) {
        return map.containsKey("name")
                || map.containsKey("match")
                || map.containsKey("set")
                || map.containsKey("rewrite")
                || map.containsKey("merge");
    }

    private String buildAnnotationSettingsYaml(AnnotationSettingsDto dto) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("posDefinitionsYaml", defaultString(dto.posDefinitionsYaml()));
        root.put("featDefinitionsYaml", defaultString(dto.featDefinitionsYaml()));
        root.put("lexiconsYaml", defaultString(dto.lexiconsYaml()));
        root.put("extractorsYaml", defaultString(dto.extractorsYaml()));
        root.put("glossMapYaml", defaultString(dto.glossMapYaml()));
        return yamlWriter.dump(root);
    }

    private AnnotationSettingsDto mapToAnnotationSettingsDto(Map<String, Object> map) {
        return new AnnotationSettingsDto(
                stringValue(map.get("posDefinitionsYaml")),
                stringValue(map.get("featDefinitionsYaml")),
                stringValue(map.get("lexiconsYaml")),
                stringValue(map.get("extractorsYaml")),
                stringValue(map.get("glossMapYaml")),
                "",
                ""
        );
    }

    private AnnotationSettingsDto mergeAnnotationSettings(AnnotationSettingsDto base, AnnotationSettingsDto incoming) {
        return new AnnotationSettingsDto(
                choose(base.posDefinitionsYaml(), incoming.posDefinitionsYaml()),
                choose(base.featDefinitionsYaml(), incoming.featDefinitionsYaml()),
                choose(base.lexiconsYaml(), incoming.lexiconsYaml()),
                choose(base.extractorsYaml(), incoming.extractorsYaml()),
                choose(base.glossMapYaml(), incoming.glossMapYaml()),
                "",
                ""
        );
    }

    private String buildEntriesCsv(List<EntryDetailDto> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,documentOrder,contextText,surfaceText,rawChujText,rawGlossText,translation,comments,correctedChujText,correctedGlossText,correctedTranslation,approved,conlluPreview\n");

        for (EntryDetailDto entry : entries) {
            sb.append(csv(entry.id() == null ? "" : entry.id().toString())).append(',')
                    .append(entry.documentOrder()).append(',')
                    .append(csv(entry.contextText())).append(',')
                    .append(csv(entry.surfaceText())).append(',')
                    .append(csv(entry.rawChujText())).append(',')
                    .append(csv(entry.rawGlossText())).append(',')
                    .append(csv(entry.translation())).append(',')
                    .append(csv(entry.comments())).append(',')
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
                    .append("(id, document_order, context_text, surface_text, raw_chuj_text, raw_gloss_text, translation, comments, corrected_chuj_text, corrected_gloss_text, corrected_translation, approved, conllu_preview) VALUES (")
                    .append(sql(id)).append(", ")
                    .append(entry.documentOrder()).append(", ")
                    .append(sql(entry.contextText())).append(", ")
                    .append(sql(entry.surfaceText())).append(", ")
                    .append(sql(entry.rawChujText())).append(", ")
                    .append(sql(entry.rawGlossText())).append(", ")
                    .append(sql(entry.translation())).append(", ")
                    .append(sql(entry.comments())).append(", ")
                    .append(sql(entry.correctedChujText())).append(", ")
                    .append(sql(entry.correctedGlossText())).append(", ")
                    .append(sql(entry.correctedTranslation())).append(", ")
                    .append(entry.approved() ? "TRUE" : "FALSE").append(", ")
                    .append(sql(entry.conlluPreview())).append(");\n");
        }

        return sb.toString().trim();
    }

    private Map<String, Object> deepStringKeyMap(Map<?, ?> source) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (var entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> subMap) {
                out.put(key, deepStringKeyMap(subMap));
            } else if (value instanceof List<?> list) {
                List<Object> mapped = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> itemMap) {
                        mapped.add(deepStringKeyMap(itemMap));
                    } else {
                        mapped.add(item);
                    }
                }
                out.put(key, mapped);
            } else {
                out.put(key, value);
            }
        }
        return out;
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

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String choose(String current, String incoming) {
        return incoming == null || incoming.isBlank() ? defaultString(current) : incoming;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}