package org.titiplex.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.titiplex.app.bridge.AppBridge;
import org.titiplex.app.service.FileDialogService;
import org.titiplex.backend.service.AnnotationConfigComposerService;
import org.titiplex.backend.service.RuleService;
import org.titiplex.backend.service.WorkspaceEntryService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = DesktopApplication.class)
class DesktopApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AppBridge appBridge;

    @Autowired
    private FileDialogService fileDialogService;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private WorkspaceEntryService workspaceEntryService;

    @Autowired
    private AnnotationConfigComposerService annotationConfigComposerService;

    @Test
    void contextShouldLoadCriticalDesktopAndBackendBeans() {
        assertNotNull(applicationContext);
        assertNotNull(appBridge);
        assertNotNull(fileDialogService);
        assertNotNull(ruleService);
        assertNotNull(workspaceEntryService);
        assertNotNull(annotationConfigComposerService);
    }

    @Test
    void bridgeBeanShouldBeUsableInsideContext() {
        String json = appBridge.ping();

        assertNotNull(json);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("pong"));
    }
}