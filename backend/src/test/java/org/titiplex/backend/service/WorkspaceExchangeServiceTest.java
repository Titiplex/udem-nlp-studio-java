package org.titiplex.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.backend.BackendApplication;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BackendApplication.class)
class WorkspaceExchangeServiceTest {

    @Autowired
    private WorkspaceExchangeService workspaceExchangeService;

    @Autowired
    private WorkspaceEntryService workspaceEntryService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private AnnotationSettingsService annotationSettingsService;

    @BeforeEach
    void setUp() {
        workspaceEntryService.importEntries(new WorkspaceImportRequestDto("""
                Ix-naq
                A1-ganar
                Il gagne.
                """, true));

        for (RuleSummaryDto rule : ruleService.listRules()) {
            // no-op here: rules might already be empty depending on the test DB lifecycle
        }
    }

    @Test
    void exportBundleJsonShouldIncludeEntriesRulesAndAnnotationSettings() {
        ruleService.saveRule(new RuleDetailDto(
                null,
                "Correction test rule",
                RuleKind.CORRECTION,
                "split",
                "token",
                true,
                10,
                "Rule used in exchange test",
                Map.of(
                        "match", Map.of("has_segment", "DIR"),
                        "set", Map.of("type", "split", "position", "end")
                ),
                """
                        - name: Correction test rule
                          scope: token
                          match:
                            has_segment: DIR
                          set:
                            type: split
                            position: end
                        """
        ));

        TextExportDto result = workspaceExchangeService.exportData(new WorkspaceExchangeRequestDto(
                "workspace_bundle_json",
                true,
                false,
                List.of("CORRECTION", "ANNOTATION"),
                true,
                true
        ));

        assertEquals("workspace-bundle.json", result.fileName());
        assertTrue(result.content().contains("\"entries\""));
        assertTrue(result.content().contains("\"rules\""));
        assertTrue(result.content().contains("\"annotationSettings\""));
        assertTrue(result.content().contains("Correction test rule"));
    }

    @Test
    void exportAnnotationSettingsYamlShouldContainMainSections() {
        AnnotationSettingsDto saved = annotationSettingsService.saveSettings(new AnnotationSettingsDto(
                "- VERB\n- NOUN",
                "- Pers[subj]\n- Number[subj]",
                "spanish_verbs:\n  - ganar",
                "agreement_verbs:\n  tag_schema: {}",
                "{}",
                "",
                ""
        ));

        assertNotNull(saved);

        TextExportDto result = workspaceExchangeService.exportData(new WorkspaceExchangeRequestDto(
                "annotation_settings_yaml",
                true,
                false,
                List.of(),
                true,
                true
        ));

        assertEquals("workspace-annotation-settings.yaml", result.fileName());
        assertTrue(result.content().contains("posDefinitionsYaml"));
        assertTrue(result.content().contains("featDefinitionsYaml"));
        assertTrue(result.content().contains("lexiconsYaml"));
        assertTrue(result.content().contains("extractorsYaml"));
        assertTrue(result.content().contains("glossMapYaml"));
    }

    @Test
    void importCorrectionRulesYamlShouldCreateCorrectionRules() {
        WorkspaceDataImportResultDto result = workspaceExchangeService.importData(
                "correction_rules_yaml",
                """
                        - name: Imported correction rule
                          scope: token
                          match:
                            has_segment: DIR
                          set:
                            type: split
                            position: end
                        """,
                false,
                true,
                false
        );

        assertEquals(0, result.importedEntries());
        assertEquals(1, result.importedRules());
        assertTrue(result.summary().contains("correction rules imported"));

        List<RuleSummaryDto> rules = ruleService.listRules();
        assertEquals(1, rules.size());
        assertEquals("CORRECTION", rules.getFirst().kind());
        assertEquals("Imported correction rule", rules.getFirst().name());
    }

    @Test
    void importAnnotationSettingsYamlShouldUpdateSettings() {
        WorkspaceDataImportResultDto result = workspaceExchangeService.importData(
                "annotation_settings_yaml",
                """
                        posDefinitionsYaml: |
                          - VERB
                          - ADV
                        featDefinitionsYaml: |
                          - Pers[subj]
                          - Number[subj]
                        lexiconsYaml: |
                          custom_lexicon:
                            - alpha
                            - beta
                        extractorsYaml: |
                          agreement_verbs:
                            tag_schema: {}
                        glossMapYaml: |
                          {}
                        """,
                false,
                false,
                true
        );

        assertEquals(0, result.importedEntries());
        assertEquals(0, result.importedRules());
        assertTrue(result.summary().contains("Annotation settings imported"));

        AnnotationSettingsDto settings = annotationSettingsService.getSettings();
        assertTrue(settings.posDefinitionsYaml().contains("VERB"));
        assertTrue(settings.posDefinitionsYaml().contains("ADV"));
        assertTrue(settings.lexiconsYaml().contains("custom_lexicon"));
    }

    @Test
    void exportEntriesSqlShouldContainStructuredColumns() {
        workspaceEntryService.saveEntry(new EntryDetailDto(
                null,
                2,
                "Context A",
                "Ix naq",
                "Ix-naq",
                "A1-ganar",
                "Il gagne.",
                "Comment A",
                "",
                "",
                "",
                false,
                ""
        ));

        TextExportDto result = workspaceExchangeService.exportData(new WorkspaceExchangeRequestDto(
                "entries_sql",
                true,
                false,
                List.of(),
                true,
                false
        ));

        assertEquals("workspace-entries.sql", result.fileName());
        assertTrue(result.content().contains("context_text"));
        assertTrue(result.content().contains("surface_text"));
        assertTrue(result.content().contains("comments"));
        assertTrue(result.content().contains("Context A"));
        assertTrue(result.content().contains("Comment A"));
    }
}