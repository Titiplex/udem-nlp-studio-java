package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.model.RuleEntity;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnotationConfigComposerService {

    private static final String BASE_CONFIG_RESOURCE = "/annotation/annotation-base.yaml";

    private final RuleRepository ruleRepository;
    private final Yaml yamlReader;
    private final Yaml yamlWriter;
    private final AnnotationConfigLoader annotationConfigLoader;

    public AnnotationConfigComposerService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        this.yamlReader = new Yaml();

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setIndicatorIndent(1);
        options.setWidth(160);

        this.yamlWriter = new Yaml(options);
        this.annotationConfigLoader = new AnnotationConfigLoader();
    }

    public AnnotationConfig buildAnnotationConfig() {
        Map<String, Object> root = loadBaseDocument();
        List<Map<String, Object>> mergedRules = ensureRulesList(root);

        ruleRepository.findByKindOrderByPriorityAscNameAsc(RuleKind.ANNOTATION).stream()
                .filter(RuleEntity::isEnabled)
                .forEach(entity -> mergedRules.addAll(extractRules(entity)));

        String mergedYaml = yamlWriter.dump(root);
        try (InputStream in = new ByteArrayInputStream(mergedYaml.getBytes(StandardCharsets.UTF_8))) {
            return annotationConfigLoader.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compose annotation config: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadBaseDocument() {
        try (InputStream in = getClass().getResourceAsStream(BASE_CONFIG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + BASE_CONFIG_RESOURCE);
            }

            Object loaded = yamlReader.load(in);
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new IllegalStateException("Base annotation config must be a YAML object");
            }

            return deepStringKeyMap(map);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load base annotation config: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> ensureRulesList(Map<String, Object> root) {
        Object current = root.get("rules");
        if (current instanceof List<?> list) {
            List<Map<String, Object>> normalized = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    normalized.add(deepStringKeyMap(map));
                }
            }
            root.put("rules", normalized);
            return normalized;
        }

        List<Map<String, Object>> rules = new ArrayList<>();
        root.put("rules", rules);
        return rules;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRules(RuleEntity entity) {
        if (entity.getRawYaml() == null || entity.getRawYaml().isBlank()) {
            return List.of();
        }

        Object loaded = yamlReader.load(entity.getRawYaml());
        if (!(loaded instanceof List<?> list)) {
            return List.of();
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }

            Map<String, Object> rule = deepStringKeyMap(map);

            rule.putIfAbsent("name", entity.getName());
            rule.putIfAbsent("scope", entity.getScope());

            if (entity.getDescription() != null && !entity.getDescription().isBlank()) {
                rule.putIfAbsent("description", entity.getDescription());
            }

            rule.putIfAbsent("priority", entity.getPriority());
            out.add(rule);
        }

        return out;
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
                    if (item instanceof Map<?, ?> map) {
                        return deepStringKeyMap(map);
                    }
                    return item;
                }).toList());
            } else {
                out.put(key, value);
            }
        }
        return out;
    }
}