package org.titiplex.backend.service;

import org.springframework.stereotype.Service;
import org.titiplex.backend.dto.ProjectDetailDto;
import org.titiplex.backend.store.jdbc.ProjectConnectionService;
import org.titiplex.backend.store.jdbc.ProjectConnectionTestResult;
import org.titiplex.backend.store.jdbc.ProjectSchemaBootstrapService;

@Service
public class ProjectProvisioningService {

    private final ProjectContextService projectContextService;
    private final ProjectConnectionService projectConnectionService;
    private final ProjectSchemaBootstrapService projectSchemaBootstrapService;

    public ProjectProvisioningService(ProjectContextService projectContextService,
                                      ProjectConnectionService projectConnectionService,
                                      ProjectSchemaBootstrapService projectSchemaBootstrapService) {
        this.projectContextService = projectContextService;
        this.projectConnectionService = projectConnectionService;
        this.projectSchemaBootstrapService = projectSchemaBootstrapService;
    }

    public ProjectConnectionTestResult testActiveProjectConnection() {
        return projectConnectionService.test(projectContextService.getRequiredActiveContext());
    }

    public ProjectDetailDto initializeActiveProjectSchema() {
        var context = projectContextService.getRequiredActiveContext();
        ProjectConnectionTestResult test = projectConnectionService.test(context);
        if (!test.success()) {
            throw new IllegalStateException("Project source connection failed: " + test.message());
        }
        projectSchemaBootstrapService.ensureProjectSchema(context);
        return projectContextService.getActiveProject();
    }
}