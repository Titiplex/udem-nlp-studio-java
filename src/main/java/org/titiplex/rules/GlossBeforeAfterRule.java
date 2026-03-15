package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GlossBeforeAfterRule implements CorrectionRule {
    private final String id;
    private final RuleSelector selector;
    private final Map<String, String> mapping;
    private final boolean ignoreCase;

    public GlossBeforeAfterRule(String id, RuleSelector selector, Map<String, String> mapping, boolean ignoreCase) {
        this.id = id;
        this.selector = selector;
        this.mapping = Map.copyOf(mapping);
        this.ignoreCase = ignoreCase;
    }

    @Override public String id() { return id; }

    @Override
    public void apply(RuleContext context) {
        for (int index : selector.select(context.alignedTokens())) {
            AlignedToken token = context.get(index);
            List<String> newGloss = new ArrayList<>();
            for (String gloss : token.glossSegments()) {
                String key = ignoreCase ? (gloss == null ? "" : gloss.toLowerCase(Locale.ROOT)) : gloss;
                newGloss.add(mapping.getOrDefault(key, gloss));
            }
            context.replace(index, context.rebuildToken(token.chujSegments(), newGloss));
        }
    }

    public static Map<String, String> buildMapping(List<String> before, List<String> after, boolean ignoreCase) {
        Map<String, String> out = new HashMap<>();
        if (after.size() == 1) {
            for (String b : before) out.put(ignoreCase ? b.toLowerCase(Locale.ROOT) : b, after.getFirst());
            return out;
        }
        for (int i = 0; i < Math.min(before.size(), after.size()); i++) {
            out.put(ignoreCase ? before.get(i).toLowerCase(Locale.ROOT) : before.get(i), after.get(i));
        }
        return out;
    }
}
