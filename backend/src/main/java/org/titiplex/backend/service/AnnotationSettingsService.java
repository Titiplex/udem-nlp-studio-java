package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.model.AnnotationSettingsEntity;
import org.titiplex.backend.repository.AnnotationSettingsRepository;
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

    public static final long SINGLETON_ID = 1L;

    private final AnnotationSettingsRepository annotationSettingsRepository;
    private final Yaml yamlReader;
    private final Yaml yamlWriter;
    private final AnnotationConfigLoader annotationConfigLoader;

    public AnnotationSettingsService(AnnotationSettingsRepository annotationSettingsRepository) {
        this.annotationSettingsRepository = annotationSettingsRepository;
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
        AnnotationSettingsEntity entity = getOrCreateDefaults();
        String baseYaml = buildBaseYaml(entity);
        String effectiveYaml = buildBaseYaml(entity);

        return new AnnotationSettingsDto(
                defaultString(entity.getPosDefinitionsYaml()),
                defaultString(entity.getFeatDefinitionsYaml()),
                defaultString(entity.getLexiconsYaml()),
                defaultString(entity.getExtractorsYaml()),
                defaultString(entity.getGlossMapYaml()),
                baseYaml,
                effectiveYaml
        );
    }

    public AnnotationSettingsDto saveSettings(AnnotationSettingsDto dto) {
        AnnotationSettingsEntity entity = annotationSettingsRepository.findById(SINGLETON_ID)
                .orElseGet(() -> new AnnotationSettingsEntity(
                        SINGLETON_ID, "", "", "", "", ""
                ));

        entity.setPosDefinitionsYaml(defaultString(dto.posDefinitionsYaml()));
        entity.setFeatDefinitionsYaml(defaultString(dto.featDefinitionsYaml()));
        entity.setLexiconsYaml(defaultString(dto.lexiconsYaml()));
        entity.setExtractorsYaml(defaultString(dto.extractorsYaml()));
        entity.setGlossMapYaml(defaultString(dto.glossMapYaml()));

        validateBaseSettings(entity);

        AnnotationSettingsEntity saved = annotationSettingsRepository.save(entity);
        String baseYaml = buildBaseYaml(saved);

        return new AnnotationSettingsDto(
                defaultString(saved.getPosDefinitionsYaml()),
                defaultString(saved.getFeatDefinitionsYaml()),
                defaultString(saved.getLexiconsYaml()),
                defaultString(saved.getExtractorsYaml()),
                defaultString(saved.getGlossMapYaml()),
                baseYaml,
                baseYaml
        );
    }

    public Map<String, Object> buildBaseDocument() {
        AnnotationSettingsEntity entity = getOrCreateDefaults();

        Map<String, Object> root = new LinkedHashMap<>();

        Map<String, Object> def = new LinkedHashMap<>();
        def.put("pos", parseYamlOrDefaultList(entity.getPosDefinitionsYaml()));
        def.put("feats", parseYamlOrDefaultList(entity.getFeatDefinitionsYaml()));

        root.put("def", def);
        root.put("lexicons", parseYamlOrDefaultMap(entity.getLexiconsYaml()));
        root.put("extractors", parseYamlOrDefaultMap(entity.getExtractorsYaml()));
        root.put("gloss_map", parseYamlOrDefaultMap(entity.getGlossMapYaml()));
        root.put("rules", List.of());

        return root;
    }

    private void validateBaseSettings(AnnotationSettingsEntity entity) {
        String yaml = buildBaseYaml(entity);
        try (InputStream in = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))) {
            AnnotationConfig config = annotationConfigLoader.load(in);
            if (config == null) {
                throw new IllegalStateException("Annotation config loader returned null");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid annotation settings: " + e.getMessage(), e);
        }
    }

    private String buildBaseYaml(AnnotationSettingsEntity entity) {
        Map<String, Object> root = new LinkedHashMap<>();

        Map<String, Object> def = new LinkedHashMap<>();
        def.put("pos", parseYamlOrDefaultList(entity.getPosDefinitionsYaml()));
        def.put("feats", parseYamlOrDefaultList(entity.getFeatDefinitionsYaml()));

        root.put("def", def);
        root.put("lexicons", parseYamlOrDefaultMap(entity.getLexiconsYaml()));
        root.put("extractors", parseYamlOrDefaultMap(entity.getExtractorsYaml()));
        root.put("gloss_map", parseYamlOrDefaultMap(entity.getGlossMapYaml()));
        root.put("rules", List.of());

        return yamlWriter.dump(root);
    }

    @SuppressWarnings("unchecked")
    private List<Object> parseYamlOrDefaultList(String raw) {
        String normalized = defaultString(raw).trim();
        if (normalized.isBlank()) {
            return List.of();
        }

        Object loaded = yamlReader.load(normalized);
        if (loaded instanceof List<?> list) {
            return (List<Object>) list;
        }

        throw new IllegalArgumentException("Expected YAML list but got: " + loaded);
    }

    private Map<String, Object> parseYamlOrDefaultMap(String raw) {
        String normalized = defaultString(raw).trim();
        if (normalized.isBlank()) {
            return Map.of();
        }

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

    private AnnotationSettingsEntity getOrCreateDefaults() {
        return annotationSettingsRepository.findById(SINGLETON_ID)
                .orElseGet(() -> annotationSettingsRepository.save(defaultEntity()));
    }

    private AnnotationSettingsEntity defaultEntity() {
        return new AnnotationSettingsEntity(
                SINGLETON_ID,
                """
                        - ADJ
                        - ADV
                        - INTJ
                        - NOUN
                        - PROPN
                        - VERB
                        - PUNCT
                        - X
                        - ADP
                        - AUX
                        - CCONJ
                        - DET
                        - NUM
                        - PART
                        - PRON
                        - SCONJ
                        """.trim(),
                """
                        - Animacy
                        - Aspect
                        - Definite
                        - Degree
                        - Mood
                        - Number
                        - Number[obj]
                        - Number[subj]
                        - Person
                        - Pers[obj]
                        - Pers[subj]
                        - Polarity
                        - Poss
                        - PronType
                        - Reflex
                        - SubCat
                        - Tense
                        - VerbForm
                        - Voice
                        """.trim(),
                """
                        spanish_verbs:
                          - ganar
                          - ir
                          - comer
                          - ver
                          - hacer
                          - venir
                          - salir
                          - decir
                          - estar
                          - tener
                          - poder
                        """.trim(),
                """
                        agreement_verbs:
                          tag_schema:
                            series:
                              A: "subj"
                              B: "obj"
                            values:
                              person: [ "1", "2", "3" ]
                              number:
                                suffix: "PL"
                        """.trim(),
                "{}"
        );
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}