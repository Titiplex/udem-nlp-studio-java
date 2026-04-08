package org.titiplex.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.titiplex.backend.service.RuleService;
import org.titiplex.backend.service.WorkspaceEntryService;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final RuleService ruleService;
    private final WorkspaceEntryService workspaceEntryService;

    public DemoDataInitializer(RuleService ruleService,
                               WorkspaceEntryService workspaceEntryService) {
        this.ruleService = ruleService;
        this.workspaceEntryService = workspaceEntryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ruleService.seedDemoRulesIfEmpty();
        workspaceEntryService.seedDemoEntriesIfEmpty();
    }
}