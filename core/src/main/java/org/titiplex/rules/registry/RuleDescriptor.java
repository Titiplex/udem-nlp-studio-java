package org.titiplex.rules.registry;

public record RuleDescriptor(
        String kind,
        String subtype,
        String label,
        String description
) {
}