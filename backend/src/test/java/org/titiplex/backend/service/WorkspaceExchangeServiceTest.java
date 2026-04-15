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
    }

    @Test
    void exportEntriesJsonShouldContainWorkspaceData() {
        TextExportDto export = workspaceExchangeService.exportData(new WorkspaceExchangeRequestDto(
                "entries_json",
                true,
                false,
                List.of(),
                false,
                false
        ));

        assertEquals("workspace-entries.json", export.fileName());
        assertTrue(export.content().contains("\"rawChujText\""));
        assertTrue(export.content().contains("Ix-naq"));
    }

    @Test
    void exportRulesYamlShouldRespectRuleKindFilter() {
        ruleService.saveRule(new RuleDetailDto(
                null,
                "Correction sample",
                RuleKind.CORRECTION,
                "split",
                "token",
                true,
                10,
                "demo",
                Map.of("match", Map.of("has_segment", "DIR")),
                """
                        - name: Correction sample
                          scope: token
                          match:
                            has_segment: DIR
                        """
        ));

        ruleService.saveRule(new RuleDetailDto(
                null,
                "Annotation sample",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                20,
                "demo",
                Map.of("set", Map.of("upos", "VERB")),
                """
                        - name: Annotation sample
                          scope: token
                          set:
                            upos: VERB
                        """
        ));

        TextExportDto export = workspaceExchangeService.exportData(new WorkspaceExchangeRequestDto(
                "correction_rules_yaml",
                true,
                false,
                List.of(),
                false,
                false
        ));

        assertEquals("workspace-correction-rules.yaml", export.fileName());
        assertTrue(export.content().contains("Correction sample"));
        assertFalse(export.content().contains("Annotation sample"));
    }

    @Test
    void importRulesJsonShouldCreateRules() {
        String json = """
                [
                  {
                    "id": null,
                    "name": "Imported annotation rule",
                    "kind": "ANNOTATION",
                    "subtype": "conllu",
                    "scope": "token",
                    "enabled": true,
                    "priority": 30,
                    "description": "imported",
                    "payload": {
                      "set": {
                        "upos": "VERB"
                      }
                    },
                    "rawYaml": "- name: Imported annotation rule\\n  scope: token\\n  set:\\n    upos: VERB\\n"
                  }
                ]
                """;

        WorkspaceDataImportResultDto result = workspaceExchangeService.importData(
                "rules_json",
                json,
                false,
                true,
                false
        );

        assertEquals(0, result.importedEntries());
        assertEquals(1, result.importedRules());
        assertTrue(result.summary().contains("rules imported from JSON"));

        assertEquals(1, ruleService.listRules().size());
        assertEquals("Imported annotation rule", ruleService.listRules().getFirst().name());
    }

    @Test
    void importWorkspaceBundleShouldCreateEntriesAndRules() {
        String json = """
                {
                  "entries": [
                    {
                      "id": null,
                      "documentOrder": 10,
                      "contextText": "",
                      "surfaceText": "",
                      "rawChujText": "Ha-ix-to",
                      "rawGlossText": "DEM-A1-ir",
                      "translation": "Celui-ci va.",
                      "comments": "",
                      "correctedChujText": "",
                      "correctedGlossText": "",
                      "correctedTranslation": "",
                      "approved": false,
                      "conlluPreview": ""
                    }
                  ],
                  "rules": [
                    {
                      "id": null,
                      "name": "Bundle rule",
                      "kind": "ANNOTATION",
                      "subtype": "conllu",
                      "scope": "token",
                      "enabled": true,
                      "priority": 50,
                      "description": "from bundle",
                      "payload": {
                        "set": {
                          "upos": "VERB"
                        }
                      },
                      "rawYaml": "- name: Bundle rule\\n  scope: token\\n  set:\\n    upos: VERB\\n"
                    }
                  ],
                  "annotationSettings": null
                }
                """;

        WorkspaceDataImportResultDto result = workspaceExchangeService.importData(
                "workspace_bundle_json",
                json,
                true,
                true,
                false
        );

        assertEquals(1, result.importedEntries());
        assertEquals(1, result.importedRules());

        assertEquals(1, workspaceEntryService.listEntries().size());
        assertEquals("Ha-ix-to", workspaceEntryService.listEntries().getFirst().rawChujText());
        assertEquals(1, ruleService.listRules().size());
        assertEquals("Bundle rule", ruleService.listRules().getFirst().name());
    }

    @Test
    void exportAnnotationSettingsYamlShouldContainCurrentBaseSections() {
        TextExportDto export = workspaceExchangeService.exportData(new WorkspaceExchangeRequestDto(
                "annotation_settings_yaml",
                true,
                false,
                List.of(),
                false,
                true
        ));

        assertEquals("workspace-annotation-settings.yaml", export.fileName());
        assertTrue(export.content().contains("posDefinitionsYaml"));
        assertTrue(export.content().contains("featDefinitionsYaml"));
        assertTrue(export.content().contains("extractorsYaml"));
    }

    @Test
    void importAnnotationSettingsJsonShouldUpdateSettings() {
        String json = """
                {
                  "posDefinitionsYaml": "- VERB\\n- NOUN",
                  "featDefinitionsYaml": "- Number\\n- Pers[subj]",
                  "lexiconsYaml": "spanish_verbs:\\n  - ganar",
                  "extractorsYaml": "agreement_verbs:\\n  tag_schema:\\n    series:\\n      A: subj",
                  "glossMapYaml": "{}",
                  "baseYamlPreview": "",
                  "effectiveYamlPreview": ""
                }
                """;

        WorkspaceDataImportResultDto result = workspaceExchangeService.importData(
                "annotation_settings_json",
                json,
                false,
                false,
                true
        );

        assertEquals(0, result.importedEntries());
        assertEquals(0, result.importedRules());
        assertTrue(result.summary().contains("Annotation settings imported from JSON"));

        AnnotationSettingsDto settings = annotationSettingsService.getSettings();
        assertTrue(settings.posDefinitionsYaml().contains("VERB"));
        assertTrue(settings.lexiconsYaml().contains("spanish_verbs"));
    }
}