package org.titiplex.backend.dto;

public record BatchCorrectionResultDto(
        int totalEntries,
        int correctedEntries,
        int skippedApprovedEntries
) {
}