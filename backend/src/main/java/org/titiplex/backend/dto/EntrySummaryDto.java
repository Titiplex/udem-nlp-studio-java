package org.titiplex.backend.dto;

import java.util.UUID;

public record EntrySummaryDto(
        UUID id,
        int documentOrder,
        String rawChujText,
        String rawGlossText,
        String translation,
        boolean approved,
        boolean hasCorrection
) {
}