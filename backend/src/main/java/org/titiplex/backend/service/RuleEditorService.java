package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.domain.rule.RuleDefinition;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.dto.RuleDraftResultDto;
import org.titiplex.backend.dto.ValidationIssueDto;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RuleEditorService {

    private final Yaml yamlReader;
    private final Yaml yamlWriter;

    public RuleEditorService() {
        this.yamlReader = new Yaml();

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setIndicatorIndent(1);
        options.setWidth(120);

        this.yamlWriter = new Yaml(options);
    }

    public RuleDraftResultDto validate(RuleDetailDto dto) {
        return new RuleDraftResultDto(dto, validateIssues(dto));
    }

    public RuleDraftResultDto parseYamlIntoDraft(RuleDetailDto dto) {
        List<ValidationIssueDto> issues = new ArrayList<>();

        if (dto.rawYaml() == null || dto.rawYaml().isBlank()) {
            issues.add(new ValidationIssueDto("rawYaml", "error", "YAML vide."));
            return new RuleDraftResultDto(dto, issues);
        }

        try {
            Object loaded = yamlReader.load(dto.rawYaml());
            if (!(loaded instanceof List<?> list) || list.isEmpty()) {
                issues.add(new ValidationIssueDto("rawYaml", "error",
                        "Le YAML doit contenir une liste avec au moins une règle."));
                return new RuleDraftResultDto(dto, issues);
            }

            Object first = list.getFirst();
            if (!(first instanceof Map<?, ?> map)) {
                issues.add(new ValidationIssueDto("rawYaml", "error",
                        "La première entrée YAML n'est pas un objet de règle."));
                return new RuleDraftResultDto(dto, issues);
            }

            Map<String, Object> payload = deepStringKeyMap(map);

            RuleDetailDto normalized = new RuleDetailDto(
                    dto.id(),
                    stringOrDefault(dto.name(), payload.get("name")),
                    dto.kind(),
                    dto.subtype(),
                    stringOrDefault(dto.scope(), payload.get("scope")),
                    dto.enabled(),
                    dto.priority(),
                    dto.description(),
                    payload,
                    dto.rawYaml()
            );

            issues.addAll(validateIssues(normalized));
            return new RuleDraftResultDto(normalized, issues);

        } catch (Exception e) {
            issues.add(new ValidationIssueDto("rawYaml", "error",
                    "YAML invalide: " + e.getMessage()));
            return new RuleDraftResultDto(dto, issues);
        }
    }

    public RuleDraftResultDto generateYamlFromDraft(RuleDetailDto dto) {
        List<ValidationIssueDto> issues = validateIssues(dto);

        Map<String, Object> yamlRule = new LinkedHashMap<>();
        yamlRule.put("name", dto.name());
        yamlRule.put("scope", dto.scope());

        if (dto.description() != null && !dto.description().isBlank()) {
            yamlRule.put("description", dto.description());
        }

        if (dto.payload() != null && !dto.payload().isEmpty()) {
            for (var entry : dto.payload().entrySet()) {
                if ("name".equals(entry.getKey()) || "scope".equals(entry.getKey()) || "description".equals(entry.getKey())) {
                    continue;
                }
                yamlRule.put(entry.getKey(), entry.getValue());
            }
        }

        String yaml = yamlWriter.dump(List.of(yamlRule));

        RuleDetailDto normalized = new RuleDetailDto(
                dto.id(),
                dto.name(),
                dto.kind(),
                dto.subtype(),
                dto.scope(),
                dto.enabled(),
                dto.priority(),
                dto.description(),
                dto.payload(),
                yaml
        );

        return new RuleDraftResultDto(normalized, issues);
    }

    public RuleDefinition toDomain(RuleDetailDto dto) {
        return new RuleDefinition(
                dto.id(),
                dto.name(),
                dto.kind(),
                dto.subtype(),
                dto.scope(),
                dto.enabled(),
                dto.priority(),
                dto.description(),
                dto.payload(),
                dto.rawYaml()
        );
    }

    private List<ValidationIssueDto> validateIssues(RuleDetailDto dto) {
        List<ValidationIssueDto> issues = new ArrayList<>();

        if (dto.name() == null || dto.name().isBlank()) {
            issues.add(new ValidationIssueDto("name", "error", "Le nom est requis."));
        }

        if (dto.kind() == null) {
            issues.add(new ValidationIssueDto("kind", "error", "Le type de règle est requis."));
        }

        if (dto.subtype() == null || dto.subtype().isBlank()) {
            issues.add(new ValidationIssueDto("subtype", "error", "Le sous-type est requis."));
        }

        if (dto.scope() == null || dto.scope().isBlank()) {
            issues.add(new ValidationIssueDto("scope", "error", "Le scope est requis."));
        }

        if (dto.payload() == null || dto.payload().isEmpty()) {
            issues.add(new ValidationIssueDto("payload", "warning", "Le payload est vide."));
            return issues;
        }

        if (!dto.payload().containsKey("match")) {
            issues.add(new ValidationIssueDto("payload.match", "warning", "Aucun bloc match défini."));
        }

        if (!dto.payload().containsKey("set")) {
            issues.add(new ValidationIssueDto("payload.set", "warning", "Aucun bloc set défini."));
        }

        return issues;
    }

    private Map<String, Object> deepStringKeyMap(Map<?, ?> source) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (var entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> subMap) {
                out.put(key, deepStringKeyMap(subMap));
            } else if (value instanceof List<?> list) {
                out.put(key, list.stream().map(item -> {
                    if (item instanceof Map<?, ?> m) {
                        return deepStringKeyMap(m);
                    }
                    return item;
                }).toList());
            } else {
                out.put(key, value);
            }
        }
        return out;
    }

    private String stringOrDefault(String current, Object candidate) {
        if (current != null && !current.isBlank()) {
            return current;
        }
        return candidate == null ? "" : String.valueOf(candidate);
    }
}