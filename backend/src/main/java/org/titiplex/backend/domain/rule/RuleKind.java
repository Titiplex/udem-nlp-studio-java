package org.titiplex.backend.domain.rule;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RuleKind {
    ANNOTATION,
    CORRECTION;

    @JsonCreator
    public static RuleKind fromValue(String value) {
        if (value == null) {
            return null;
        }
        return RuleKind.valueOf(value.trim().toUpperCase());
    }
}
