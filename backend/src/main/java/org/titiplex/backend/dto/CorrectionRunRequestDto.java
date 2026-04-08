package org.titiplex.backend.dto;

import java.util.UUID;

public record CorrectionRunRequestDto(
        UUID entryId,
        boolean force
) {
}