package org.titiplex.backend.dto;

public record WorkspaceImportResultDto(
        int importedEntries,
        int totalEntries
) {
}