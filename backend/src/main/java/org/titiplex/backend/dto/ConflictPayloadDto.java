package org.titiplex.backend.dto;

public record ConflictPayloadDto(
        String entityType,
        String entityId,
        Long expectedVersion,
        String message
) {
}