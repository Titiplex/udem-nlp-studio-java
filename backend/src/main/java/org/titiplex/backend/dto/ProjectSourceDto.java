package org.titiplex.backend.dto;

public record ProjectSourceDto(
        String id,
        String kind,
        String host,
        Integer port,
        String database,
        String schema,
        boolean ssl,
        boolean secretsConfigured
) {
}