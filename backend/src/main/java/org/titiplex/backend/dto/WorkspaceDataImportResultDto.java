package org.titiplex.backend.dto;

public record WorkspaceDataImportResultDto(
        int importedEntries,
        int importedRules,
        String summary
) {
}