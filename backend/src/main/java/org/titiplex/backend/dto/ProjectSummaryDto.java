package org.titiplex.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectSummaryDto(
        UUID projectId,
        String name,
        boolean active,
        String sourceKind,
        String sourceLabel,
        Instant lastOpenedAt
) {
}