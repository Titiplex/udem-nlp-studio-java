package org.titiplex.backend.dto;

public record ProjectConnectionStatusDto(
        boolean success,
        String message
) {
}