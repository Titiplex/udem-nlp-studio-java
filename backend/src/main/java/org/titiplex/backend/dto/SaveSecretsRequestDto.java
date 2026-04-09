package org.titiplex.backend.dto;

public record SaveSecretsRequestDto(
        String projectId,
        String usernameRef,
        String username,
        String passwordRef,
        String password
) {
}