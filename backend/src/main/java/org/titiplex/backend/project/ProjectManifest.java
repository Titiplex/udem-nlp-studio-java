package org.titiplex.backend.project;

import java.util.List;
import java.util.UUID;

public record ProjectManifest(
        UUID projectId,
        String name,
        String version,
        String defaultSourceId,
        List<ProjectMember> members,
        List<ProjectSourceDefinition> sources
) {
}