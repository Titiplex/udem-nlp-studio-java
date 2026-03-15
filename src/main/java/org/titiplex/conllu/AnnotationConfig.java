package org.titiplex.conllu;

import java.util.ArrayList;
import java.util.List;

public final class AnnotationConfig {
    private final GlossMapper glossMapper = new GlossMapper();
    private final List<AnnotationRule> rules = new ArrayList<>();

    public GlossMapper glossMapper() {
        return glossMapper;
    }

    public List<AnnotationRule> rules() {
        return rules;
    }
}
