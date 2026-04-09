package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.concurrency.SaveOptions;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.project.ProjectContext;
import org.titiplex.backend.store.AnnotationSettingsStore;
import org.titiplex.conllu.AnnotationConfig;
import org.titiplex.conllu.AnnotationConfigLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnotationSettingsService {

    private final ProjectContextService projectContextService;
    private final AnnotationSettingsStore annotationSettingsStore;
    private final Yaml yamlReader;
    private final Yaml yamlWriter;
    private final AnnotationConfigLoader annotationConfigLoader;

    public AnnotationSettingsService(ProjectContextService projectContextService,
                                     AnnotationSettingsStore annotationSettingsStore) {
        this.projectContextService = projectContextService;
        this.annotationSettingsStore = annotationSettingsStore;
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

    public AnnotationSettingsDto getSettings() {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        AnnotationSettingsDto entity = annotationSettingsStore.getSettings(project);
        String baseYaml = buildBaseYaml(entity);
        return new AnnotationSettingsDto(
                defaultString(entity.posDefinitionsYaml()),
                defaultString(entity.featDefinitionsYaml()),
                defaultString(entity.lexiconsYaml()),
                defaultString(entity.extractorsYaml()),
                defaultString(entity.glossMapYaml()),
                baseYaml,
                baseYaml,
                entity.version(),
                entity.updatedBy(),
                entity.updatedAt()
        );
    }

    public AnnotationSettingsDto saveSettings(AnnotationSettingsDto dto) {
        ProjectContext project = projectContextService.getRequiredActiveContext();
        validateBaseSettings(dto);
        AnnotationSettingsDto saved = annotationSettingsStore.saveSettings(
                project,
                dto,
                SaveOptions.standard(project.localMember().principalId())
        );
        String baseYaml = buildBaseYaml(saved);
        return new AnnotationSettingsDto(
                defaultString(saved.posDefinitionsYaml()),
                defaultString(saved.featDefinitionsYaml()),
                defaultString(saved.lexiconsYaml()),
                defaultString(saved.extractorsYaml()),
                defaultString(saved.glossMapYaml()),
                baseYaml,
                baseYaml,
                saved.version(),
                saved.updatedBy(),
                saved.updatedAt()
        );
    }

    public Map<String, Object> buildBaseDocument() {
        AnnotationSettingsDto entity = getSettings();
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("pos", parseYamlOrDefaultList(entity.posDefinitionsYaml()));
        def.put("feats", parseYamlOrDefaultList(entity.featDefinitionsYaml()));
        root.put("def", def);
        root.put("lexicons", parseYamlOrDefaultMap(entity.lexiconsYaml()));
        root.put("extractors", parseYamlOrDefaultMap(entity.extractorsYaml()));
        root.put("gloss_map", parseYamlOrDefaultMap(entity.glossMapYaml()));
        root.put("rules", List.of());
        return root;
    }

    private void validateBaseSettings(AnnotationSettingsDto dto) {
        String yaml = buildBaseYaml(dto);
        try (InputStream in = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {
            AnnotationConfig config = annotationConfigLoader.load(in);
            if (config == null) {
                throw new IllegalStateException("Annotation config loader returned null");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid annotation settings: " + e.getMessage(), e);
        }
    }

    private String buildBaseYaml(AnnotationSettingsDto entity) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("pos", parseYamlOrDefaultList(entity.posDefinitionsYaml()));
        def.put("feats", parseYamlOrDefaultList(entity.featDefinitionsYaml()));
        root.put("def", def);
        root.put("lexicons", parseYamlOrDefaultMap(entity.lexiconsYaml()));
        root.put("extractors", parseYamlOrDefaultMap(entity.extractorsYaml()));
        root.put("gloss_map", parseYamlOrDefaultMap(entity.glossMapYaml()));
        root.put("rules", List.of());
        return yamlWriter.dump(root);
    }

    @SuppressWarnings("unchecked")
    private List<Object> parseYamlOrDefaultList(String raw) {
        String normalized = defaultString(raw).trim();
        if (normalized.isBlank()) return List.of();
        Object loaded = yamlReader.load(normalized);
        if (loaded instanceof List<?> list) return (List<Object>) list;
        throw new IllegalArgumentException("Expected YAML list but got: " + loaded);
    }

    private Map<String, Object> parseYamlOrDefaultMap(String raw) {
        String normalized = defaultString(raw).trim();
        if (normalized.isBlank()) return Map.of();
        Object loaded = yamlReader.load(normalized);
        if (loaded instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (var entry : map.entrySet()) {
                out.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return out;
        }
        throw new IllegalArgumentException("Expected YAML object but got: " + loaded);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}