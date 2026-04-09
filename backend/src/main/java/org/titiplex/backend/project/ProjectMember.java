package org.titiplex.backend.project;

public record ProjectMember(
        String principalId,
        String displayName,
        ProjectRole role
) {
}