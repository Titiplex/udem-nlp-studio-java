package org.titiplex.app.bridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.titiplex.app.DesktopApplication;
import org.titiplex.backend.repository.AnnotationSettingsRepository;
import org.titiplex.backend.repository.RuleRepository;
import org.titiplex.backend.repository.WorkspaceEntryRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = DesktopApplication.class)
class AppBridgeContextIT {

    @Autowired
    private AppBridge appBridge;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private WorkspaceEntryRepository workspaceEntryRepository;

    @Autowired
    private AnnotationSettingsRepository annotationSettingsRepository;

    @BeforeEach
    void setUp() {
        workspaceEntryRepository.deleteAll();
        ruleRepository.deleteAll();
        annotationSettingsRepository.deleteAll();
    }

    @Test
    void getAppInfoShouldReturnSuccessfulJsonPayload() {
        String json = appBridge.getAppInfo();

        assertNotNull(json);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("NLP Studio"));
        assertTrue(json.contains("0.1.0"));
    }

    @Test
    void listEntriesShouldReturnSuccessfulEmptyListPayload() {
        String json = appBridge.listEntries();

        assertNotNull(json);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\""));
    }

    @Test
    void listRulesShouldReturnSuccessfulEmptyListPayload() {
        String json = appBridge.listRules();

        assertNotNull(json);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\""));
    }

    @Test
    void getAnnotationSettingsShouldReturnBaseAndEffectiveYaml() {
        String json = appBridge.getAnnotationSettings();

        assertNotNull(json);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("posDefinitionsYaml"));
        assertTrue(json.contains("effectiveYamlPreview"));
    }

    @Test
    void malformedSaveEntryPayloadShouldReturnStructuredError() {
        String json = appBridge.saveEntry("{not-json}");

        assertNotNull(json);
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("Save entry failed"));
    }
}