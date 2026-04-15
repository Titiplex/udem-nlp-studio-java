package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.repository.AnnotationSettingsRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class AnnotationSettingsServiceTest {

    @Autowired
    private AnnotationSettingsService annotationSettingsService;

    @Autowired
    private AnnotationSettingsRepository annotationSettingsRepository;

    @BeforeEach
    void setUp() {
        annotationSettingsRepository.deleteAll();
    }

    @Test
    void getSettingsShouldCreateDefaultSingletonWhenMissing() {
        AnnotationSettingsDto dto = annotationSettingsService.getSettings();

        assertNotNull(dto);
        assertTrue(dto.posDefinitionsYaml().contains("VERB"));
        assertTrue(dto.featDefinitionsYaml().contains("Pers[subj]"));
        assertTrue(dto.lexiconsYaml().contains("spanish_verbs"));
        assertTrue(dto.baseYamlPreview().contains("def:"));
        assertTrue(annotationSettingsRepository.findById(AnnotationSettingsService.SINGLETON_ID).isPresent());
    }

    @Test
    void saveSettingsShouldPersistAndReturnUpdatedBaseYaml() {
        AnnotationSettingsDto saved = annotationSettingsService.saveSettings(new AnnotationSettingsDto(
                "- VERB\n- NOUN",
                "- Number\n- Pers[subj]",
                "spanish_verbs:\n  - ganar",
                """
                        agreement_verbs:
                          tag_schema:
                            series:
                              A: "subj"
                            values:
                              person: [ "1", "2", "3" ]
                              number:
                                suffix: "PL"
                        """,
                "{}",
                "",
                ""
        ));

        assertTrue(saved.posDefinitionsYaml().contains("VERB"));
        assertTrue(saved.baseYamlPreview().contains("lexicons:"));
        assertTrue(saved.baseYamlPreview().contains("extractors:"));

        AnnotationSettingsDto reloaded = annotationSettingsService.getSettings();
        assertTrue(reloaded.lexiconsYaml().contains("ganar"));
    }

    @Test
    void saveSettingsShouldRejectInvalidYamlShapes() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> annotationSettingsService.saveSettings(new AnnotationSettingsDto(
                        "not-a-list",
                        "- Number",
                        "spanish_verbs:\n  - ganar",
                        "agreement_verbs:\n  tag_schema: {}",
                        "{}",
                        "",
                        ""
                ))
        );

        String message = ex.getMessage() == null ? "" : ex.getMessage();
        String causeMessage = ex.getCause() != null && ex.getCause().getMessage() != null
                ? ex.getCause().getMessage()
                : "";

        assertTrue(
                message.contains("Invalid annotation settings")
                        || message.contains("Expected YAML list")
                        || causeMessage.contains("Expected YAML list"),
                "Unexpected exception message: " + message + " / cause: " + causeMessage
        );
    }

    @Test
    void buildBaseDocumentShouldExposeExpectedSections() {
        var baseDocument = annotationSettingsService.buildBaseDocument();

        assertTrue(baseDocument.containsKey("def"));
        assertTrue(baseDocument.containsKey("lexicons"));
        assertTrue(baseDocument.containsKey("extractors"));
        assertTrue(baseDocument.containsKey("gloss_map"));
        assertTrue(baseDocument.containsKey("rules"));
    }
}