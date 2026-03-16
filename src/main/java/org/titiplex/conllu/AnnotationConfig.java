package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnnotationConfig {
    private final GlossMapper glossMapper = new GlossMapper();
    private final List<AnnotationRule> rules = new ArrayList<>();
    private final Map<String, ExtractorDef> extractors = new LinkedHashMap<>();
    private final Set<String> posDefinitions = new LinkedHashSet<>();
    private final Set<String> featDefinitions = new LinkedHashSet<>();

    public GlossMapper glossMapper() {
        return glossMapper;
    }

    public List<AnnotationRule> rules() {
        return rules;
    }

    public Map<String, ExtractorDef> extractors() {
        return extractors;
    }

    public Set<String> posDefinitions() {
        return posDefinitions;
    }

    public Set<String> featDefinitions() {
        return featDefinitions;
    }

    public void applyExtractor(String name, AlignedToken token, ConlluLine line, Map<String, Object> ctx) {
        ExtractorDef ex = extractors.get(name);
        if (ex == null) return;
        Map<String, Object> intoMap = extractAgreement(token);
        if (intoMap.isEmpty()) return;
        ctx.put(name, intoMap);
        if (line == null) return;
        for (RoutingRule rr : ex.routing()) {
            if (!ConditionEvaluator.evaluate(rr.when(), intoMap)) continue;
            Map<String, String> feats = new LinkedHashMap<>();
            for (var e : rr.set().entrySet()) {
                String resolved = TemplateResolver.resolvePath(intoMap, e.getValue());
                if (!resolved.isBlank()) feats.put(e.getKey(), resolved);
            }
            line.putAllFeats(feats, false);
            break;
        }
    }

    private Map<String, Object> extractAgreement(AlignedToken token) {
        Map<String, Object> out = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("^([AB])([123])(SG|PL)?$", Pattern.CASE_INSENSITIVE);
        for (String gloss : token.glossSegments()) {
            Matcher m = pattern.matcher(gloss.toUpperCase(Locale.ROOT));
            if (!m.matches()) continue;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("person", m.group(2));
            values.put("number", "PL".equalsIgnoreCase(m.group(3)) ? "Plur" : "Sing");
            out.put(m.group(1).toUpperCase(Locale.ROOT), values);
        }
        return out;
    }

    public record ExtractorDef(String name, List<RoutingRule> routing) {
    }

    public record RoutingRule(String when, Map<String, String> set) {
    }
}
