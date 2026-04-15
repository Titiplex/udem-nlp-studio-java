package org.titiplex.app.bridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.app.service.FileDialogService;
import org.titiplex.backend.domain.rule.RuleKind;
import org.titiplex.backend.dto.*;
import org.titiplex.backend.service.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppBridgeTest {

    @Mock
    private RuleService ruleService;
    @Mock
    private RuleSchemaService ruleSchemaService;
    @Mock
    private RuleEditorService ruleEditorService;
    @Mock
    private WorkspaceEntryService workspaceEntryService;
    @Mock
    private WorkspaceExchangeService workspaceExchangeService;
    @Mock
    private AnnotationSettingsService annotationSettingsService;
    @Mock
    private AnnotationConfigComposerService annotationConfigComposerService;
    @Mock
    private FileDialogService fileDialogService;

    private AppBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new AppBridge(
                ruleService,
                ruleSchemaService,
                ruleEditorService,
                workspaceEntryService,
                workspaceExchangeService,
                annotationSettingsService,
                annotationConfigComposerService,
                fileDialogService
        );
    }

    @Test
    void pingShouldReturnSuccessPayload() {
        String json = bridge.ping();

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("pong"));
    }

    @Test
    void listRulesShouldSerializeServiceResult() {
        UUID id = UUID.randomUUID();

        when(ruleService.listRules()).thenReturn(List.of(
                new RuleSummaryDto(
                        id,
                        "Test rule",
                        RuleKind.ANNOTATION,
                        "conllu",
                        "token",
                        true,
                        10
                )
        ));

        String json = bridge.listRules();

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Test rule"));
        assertTrue(json.contains("ANNOTATION"));
    }

    @Test
    void getRuleShouldReturnErrorForInvalidUuid() {
        String json = bridge.getRule("not-a-uuid");

        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("Cannot load rule"));
    }

    @Test
    void saveWorkspaceExportShouldCallFileDialogAndReturnSavedPath() {
        WorkspaceExchangeRequestDto request = new WorkspaceExchangeRequestDto(
                "rules_json",
                true,
                false,
                List.of("ANNOTATION"),
                true,
                true
        );

        when(workspaceExchangeService.exportData(any())).thenReturn(
                new TextExportDto("workspace-rules.json", "{\"demo\":true}")
        );
        when(fileDialogService.saveTextFile(anyString(), anyString(), anyString(), anyList()))
                .thenReturn("/tmp/workspace-rules.json");

        String payload = """
                {
                  "format":"rules_json",
                  "preferCorrected":true,
                  "correctedOnly":false,
                  "ruleKinds":["ANNOTATION"],
                  "onlyEnabledRules":true,
                  "includeAnnotationSettings":true
                }
                """;

        String json = bridge.saveWorkspaceExport(payload);

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("/tmp/workspace-rules.json"));
    }

    @Test
    void importWorkspaceFromFileShouldReturnErrorWhenNoContentSelected() {
        when(fileDialogService.openTextFile(anyString(), anyList())).thenReturn("");

        String payload = """
                {
                  "format":"rules_json",
                  "replaceExistingEntries":false,
                  "replaceExistingRules":true,
                  "replaceAnnotationSettings":false
                }
                """;

        String json = bridge.importWorkspaceFromFile(payload);

        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("No file selected or file empty"));
    }

    @Test
    void getAnnotationSettingsShouldIncludeEffectivePreview() {
        when(annotationSettingsService.getSettings()).thenReturn(new AnnotationSettingsDto(
                "- VERB",
                "- Number",
                "spanish_verbs:\n  - ganar",
                "agreement_verbs:\n  tag_schema: {}",
                "{}",
                "base-preview",
                "old-effective-preview"
        ));
        when(annotationConfigComposerService.buildEffectiveYamlPreview()).thenReturn("effective-preview");

        String json = bridge.getAnnotationSettings();

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("base-preview"));
        assertTrue(json.contains("effective-preview"));
    }

    @Test
    void runCorrectionShouldReturnErrorOnMalformedJson() {
        String json = bridge.runCorrection("{not-json}");

        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("Correction failed"));
    }

    @Test
    void saveRuleShouldReturnValidatedDraft() {
        RuleDetailDto saved = new RuleDetailDto(
                UUID.randomUUID(),
                "Saved rule",
                RuleKind.ANNOTATION,
                "conllu",
                "token",
                true,
                42,
                "desc",
                Map.of("set", Map.of("upos", "VERB")),
                "- name: Saved rule"
        );

        RuleDraftResultDto validation = new RuleDraftResultDto(
                saved,
                List.of()
        );

        when(ruleService.saveRule(any())).thenReturn(saved);
        when(ruleEditorService.validate(saved)).thenReturn(validation);

        String payload = """
                {
                  "id": null,
                  "name": "Saved rule",
                  "kind": "ANNOTATION",
                  "subtype": "conllu",
                  "scope": "token",
                  "enabled": true,
                  "priority": 42,
                  "description": "desc",
                  "payload": {
                    "set": {
                      "upos": "VERB"
                    }
                  },
                  "rawYaml": "- name: Saved rule"
                }
                """;

        String json = bridge.saveRule(payload);

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Saved rule"));
        assertTrue(json.contains("VERB"));
    }
}