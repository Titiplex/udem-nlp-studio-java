package org.titiplex.backend.service;

import org.springframework.stereotype.Service;

@Service
public class ProjectBootstrapService {

    private final ProjectContextService projectContextService;

    public ProjectBootstrapService(ProjectContextService projectContextService) {
        this.projectContextService = projectContextService;
    }

    public void ensureLocalIdentityExists() {
        projectContextService.getOrCreateLocalIdentity();
    }
}