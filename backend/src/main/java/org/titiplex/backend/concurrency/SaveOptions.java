package org.titiplex.backend.concurrency;

public record SaveOptions(
        boolean force,
        String updatedBy
) {
    public static SaveOptions standard(String updatedBy) {
        return new SaveOptions(false, updatedBy);
    }
}