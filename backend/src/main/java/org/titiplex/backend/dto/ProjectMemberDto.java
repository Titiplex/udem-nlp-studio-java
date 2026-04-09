package org.titiplex.backend.dto;

public record ProjectMemberDto(
        String principalId,
        String displayName,
        String role
) {
}