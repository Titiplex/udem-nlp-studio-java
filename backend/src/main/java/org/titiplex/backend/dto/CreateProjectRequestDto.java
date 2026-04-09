package org.titiplex.backend.dto;

public record CreateProjectRequestDto(
        String name,
        String directory,
        String sourceId,
        String host,
        Integer port,
        String database,
        String schema,
        boolean ssl,
        String username,
        String password
) {
}