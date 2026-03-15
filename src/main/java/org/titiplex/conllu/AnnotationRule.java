package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class AnnotationRule {
    private final String name;
    private final String scope;
    private final Pattern regex;
    private final Set<String> inList;
    private final boolean onGloss;
    private final String glossSpecial;
    private final String upos;
    private final Map<String, String> feats;
    private final List<Map<String, String>> extracts;

    public AnnotationRule(String name, String scope, Pattern regex, Set<String> inList, boolean onGloss, String glossSpecial, String upos, Map<String, String> feats, List<Map<String, String>> extracts) {
        this.name = name == null ? "" : name;
        this.scope = scope == null || scope.isBlank() ? "token" : scope;
        this.regex = regex;
        this.inList = inList == null ? Set.of() : Set.copyOf(inList);
        this.onGloss = onGloss;
        this.glossSpecial = glossSpecial;
        this.upos = upos;
        this.feats = feats == null ? Map.of() : Map.copyOf(feats);
        this.extracts = extracts == null ? List.of() : List.copyOf(extracts);
    }

    public boolean matches(AlignedToken tok) {
        if ("spanish_verbs".equalsIgnoreCase(glossSpecial) && !hasSpanishVerb(tok)) return false;
        String surface = onGloss ? tok.glossSurface() : tok.chujSurface();
        if (regex != null && surface != null && regex.matcher(surface).find()) return true;
        if (!inList.isEmpty()) {
            for (String s : (onGloss ? tok.glossSegments() : tok.chujSegments())) {
                if (inList.contains(s)) return true;
            }
            return false;
        }
        return regex == null && inList.isEmpty() && (glossSpecial == null || hasSpanishVerb(tok));
    }

    public void apply(ConlluLine line, AlignedToken tok, AnnotationConfig config) {
        if (upos != null && !upos.isBlank() && "_".equals(line.upos())) line.setUpos(upos);
        line.putAllFeats(feats, false);
        for (Map<String, String> ex : extracts) {
            if ("scan_agreement".equalsIgnoreCase(ex.get("type"))) {
                config.applyExtractor(ex.getOrDefault("extractor", "agreement_verbs"), tok, line);
            }
        }
    }

    public String scope() {
        return scope;
    }

    public String name() {
        return name;
    }

    private boolean hasSpanishVerb(AlignedToken tok) {
        for (String g : tok.glossSegments()) {
            String lower = g.toLowerCase(Locale.ROOT);
            if (g.equals(lower) && (lower.matches(".*(ar|er|ir)$") || List.of("estar", "ser", "ir", "venir", "hacer", "decir", "dar", "ver", "poder", "tener", "regresar", "existir", "exist").contains(lower))) {
                return true;
            }
        }
        return false;
    }
}
