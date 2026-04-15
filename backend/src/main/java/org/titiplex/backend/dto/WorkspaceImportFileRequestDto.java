package org.titiplex.backend.dto;

public record WorkspaceImportFileRequestDto(
        String format,
        boolean replaceExistingEntries,
        boolean replaceExistingRules,
        boolean replaceAnnotationSettings
) {
}