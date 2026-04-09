package org.titiplex.backend.project;

public record ProjectSourceDefinition(
        String id,
        ProjectSourceKind kind,
        String host,
        Integer port,
        String database,
        String schema,
        String usernameRef,
        String passwordRef,
        boolean ssl
) {
}