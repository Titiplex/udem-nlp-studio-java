package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.AnnotationSettingsDto;
import org.titiplex.backend.dto.RuleDetailDto;
import org.titiplex.backend.repository.AnnotationSettingsRepository;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.conllu.AnnotationConfig;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class AnnotationConfigComposerServiceTest {

    @Autowired
    private AnnotationConfigComposerService annotationConfigComposerService;

    @Autowired
    private AnnotationSettingsService annotationSettingsService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private AnnotationSettingsRepository annotationSettingsRepository;

    @BeforeEach
    void setUp() {
        ruleRepository.deleteAll();
        annotationSettingsRepository.deleteAll();

        annotationSettingsService.saveSettings(new AnnotationSettingsDto(
                "- VERB\n- DET\n- NOUN",
                "- Pers[subj]\n- Number[subj]",
                "spanish_verbs:\n  - ganar\n  - ir",
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
                        """,
                "{}",
                "",
                ""
        ));
    }

    @Test
    void buildEffectiveYamlPreviewShouldMergeEnabledAnnotationRulesOnly() {
        ruleService.saveRule(new RuleDetailDto(
                null,
                "Enabled annotation",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                10,
                "desc",
                Map.of("set", Map.of("upos", "VERB")),
                """
                        - name: Enabled annotation
                          scope: token
                          set:
                            upos: VERB
                        """
        ));

        ruleService.saveRule(new RuleDetailDto(
                null,
                "Disabled annotation",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                false,
                20,
                "desc",
                Map.of("set", Map.of("upos", "DET")),
                """
                        - name: Disabled annotation
                          scope: token
                          set:
                            upos: DET
                        """
        ));

        ruleService.saveRule(new RuleDetailDto(
                null,
                "Correction rule",
                RuleKind.CORRECTION,
                "split",
                "token",
                true,
                5,
                "desc",
                Map.of("match", Map.of("has_segment", "DIR")),
                """
                        - name: Correction rule
                          scope: token
                          match:
                            has_segment: DIR
                        """
        ));

        String preview = annotationConfigComposerService.buildEffectiveYamlPreview();

        assertTrue(preview.contains("Enabled annotation"));
        assertFalse(preview.contains("Disabled annotation"));
        assertFalse(preview.contains("Correction rule"));
        assertTrue(preview.contains("lexicons:"));
        assertTrue(preview.contains("extractors:"));
    }

    @Test
    void buildAnnotationConfigShouldReturnUsableConfig() {
        ruleService.saveRule(new RuleDetailDto(
                null,
                "Agreement rule",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                10,
                "desc",
                Map.of(
                        "match", Map.of("gloss", Map.of("in_lexicon", "spanish_verbs")),
                        "set", Map.of(
                                "upos", "VERB",
                                "extract", java.util.List.of(Map.of(
                                        "type", "scan_agreement",
                                        "extractor", "agreement_verbs",
                                        "into", "agreement_verbs"
                                )),
                                "feats_template", Map.of(
                                        "Pers[subj]", "{agreement_verbs.A.person}",
                                        "Number[subj]", "{agreement_verbs.A.number}"
                                )
                        )
                ),
                """
                        - name: Agreement rule
                          scope: token
                          match:
                            gloss:
                              in_lexicon: spanish_verbs
                          set:
                            upos: VERB
                            extract:
                              - type: scan_agreement
                                extractor: agreement_verbs
                                into: agreement_verbs
                            feats_template:
                              Pers[subj]: "{agreement_verbs.A.person}"
                              Number[subj]: "{agreement_verbs.A.number}"
                        """
        ));

        AnnotationConfig config = annotationConfigComposerService.buildAnnotationConfig();

        assertNotNull(config);
        assertTrue(config.posDefinitions().contains("VERB"));
        assertTrue(config.featDefinitions().contains("Pers[subj]"));
        assertTrue(config.lexiconRegistry().contains("spanish_verbs", "ganar"));
        assertFalse(config.rules().isEmpty());
    }
}