package org.titiplex.backend.store.jdbc;

public record ProjectConnectionTestResult(
        boolean success,
        String message
) {
}