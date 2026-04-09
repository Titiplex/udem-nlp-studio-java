package org.titiplex.backend.project;

import java.nio.file.Path;
import java.util.UUID;

public record ProjectContext(
        UUID projectId,
        String name,
        Path manifestPath,
        ProjectManifest manifest,
        ProjectMember localMember,
        ProjectSourceDefinition defaultSource
) {
}