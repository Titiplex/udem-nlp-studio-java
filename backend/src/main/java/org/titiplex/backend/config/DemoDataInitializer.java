package org.titiplex.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.titiplex.backend.service.ProjectBootstrapService;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final ProjectBootstrapService projectBootstrapService;

    public DemoDataInitializer(ProjectBootstrapService projectBootstrapService) {
        this.projectBootstrapService = projectBootstrapService;
    }

    @Override
    public void run(ApplicationArguments args) {
        projectBootstrapService.ensureLocalIdentityExists();
    }
}