package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.repository.AnnotationSettingsRepository;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;
import org.titiplex.conllu.AnnotationConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class AnnotationSettingsServiceTest {

    @Autowired
    private AnnotationSettingsService annotationSettingsService;

    @Autowired
    private AnnotationConfigComposerService annotationConfigComposerService;

    @Autowired
    private AnnotationSettingsRepository annotationSettingsRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private WorkspaceEntryRepository workspaceEntryRepository;

    @BeforeEach
    void setUp() {
        workspaceEntryRepository.deleteAll();
        ruleRepository.deleteAll();
        annotationSettingsRepository.deleteAll();
    }

    @Test
    void shouldLoadDefaultSettings() {
        AnnotationSettingsDto dto = annotationSettingsService.getSettings();

        assertNotNull(dto);
        assertTrue(dto.posDefinitionsYaml().contains("VERB"));
        assertTrue(dto.lexiconsYaml().contains("spanish_verbs"));
        assertFalse(dto.baseYamlPreview().isBlank());
    }

    @Test
    void shouldSaveCustomSettings() {
        AnnotationSettingsDto saved = annotationSettingsService.saveSettings(new AnnotationSettingsDto(
                "- VERB\n- NOUN",
                "- Pers[subj]\n- Number[subj]",
                """
                        spanish_verbs:
                          - ganar
                          - ir
                        """.trim(),
                """
                        agreement_verbs:
                          tag_schema:
                            series:
                              A: "subj"
                            values:
                              person: [ "1", "2", "3" ]
                              number:
                                suffix: "PL"
                        """.trim(),
                "{}",
                "",
                ""
        ));

        assertTrue(saved.posDefinitionsYaml().contains("VERB"));
        assertTrue(saved.baseYamlPreview().contains("spanish_verbs"));
    }

    @Test
    void composerShouldBuildExecutableAnnotationConfigFromSavedSettings() {
        annotationSettingsService.saveSettings(new AnnotationSettingsDto(
                "- VERB\n- NOUN",
                "- Pers[subj]\n- Number[subj]",
                """
                        spanish_verbs:
                          - ganar
                        """.trim(),
                """
                        agreement_verbs:
                          tag_schema:
                            series:
                              A: "subj"
                            values:
                              person: [ "1", "2", "3" ]
                              number:
                                suffix: "PL"
                        """.trim(),
                "{}",
                "",
                ""
        ));

        AnnotationConfig config = annotationConfigComposerService.buildAnnotationConfig();

        assertNotNull(config);
        assertTrue(config.posDefinitions().contains("VERB"));
        assertTrue(config.featDefinitions().contains("Pers[subj]"));
        assertTrue(config.lexiconRegistry().contains("spanish_verbs", "ganar"));
        assertTrue(config.extractors().containsKey("agreement_verbs"));
    }
}