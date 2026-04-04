package org.titiplex.backend.dto;

public record WorkspaceImportRequestDto(
        String rawText,
        boolean replaceExisting
) {
}