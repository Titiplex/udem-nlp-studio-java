package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;

public final class SplitDirectionalRule implements CorrectionRule {
    private final String id;
    private final List<String> suffixes;

    public SplitDirectionalRule(String id, List<String> suffixes) { this.id = id; this.suffixes = suffixes; }
    @Override public String id() { return id; }

    @Override
    public void apply(RuleContext context) {
        int i = 0;
        while (i < context.size()) {
            AlignedToken tok = context.get(i);
            if (tok.glossSegments().stream().noneMatch(g -> g.equalsIgnoreCase("DIR") || g.toLowerCase(Locale.ROOT).startsWith("dir."))) { i++; continue; }
            String s = tok.chujSurface();
            String matched = suffixes.stream().filter(sf -> !sf.isBlank() && s.toLowerCase(Locale.ROOT).endsWith(sf.toLowerCase(Locale.ROOT)) && s.length() > sf.length()).findFirst().orElse(null);
            if (matched == null) { i++; continue; }
            String root = s.substring(0, s.length() - matched.length());
            AlignedToken left = context.rebuildToken(List.of(root), List.of("_"));
            AlignedToken right = context.rebuildToken(List.of(matched), tok.glossSegments());
            context.replace(i, left);
            context.alignedTokensMutable().add(i + 1, right);
            i += 2;
        }
    }
}
