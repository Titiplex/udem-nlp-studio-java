package org.titiplex.backend.dto;

public record WorkspaceExportRequestDto(
        boolean preferCorrected,
        boolean correctedOnly
) {
}