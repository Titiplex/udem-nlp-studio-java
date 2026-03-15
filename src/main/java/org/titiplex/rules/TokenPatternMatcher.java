package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.List;
import java.util.Locale;

public final class TokenPatternMatcher {
    private TokenPatternMatcher() {
    }

    public static boolean matchesAt(List<AlignedToken> tokens, int i, MatchSpec spec) {
        if (i < 0 || i >= tokens.size()) return false;
        AlignedToken t = tokens.get(i);
        if (!spec.glossValues().isEmpty() && spec.glossValues().stream().noneMatch(g -> containsIgnoreCase(t.glossSegments(), g) || eq(t.glossSurface(), g)))
            return false;
        if (!spec.glossStartsWith().isEmpty() && spec.glossStartsWith().stream().noneMatch(g -> t.glossSegments().stream().anyMatch(s -> norm(s).startsWith(norm(g)))))
            return false;
        if (!spec.tokenIsWord().isEmpty() && spec.tokenIsWord().stream().noneMatch(w -> eq(t.chujSurface(), w)))
            return false;
        if (!spec.tokenAny().isEmpty() && spec.tokenAny().stream().noneMatch(w -> containsIgnoreCase(t.chujSegments(), w) || eq(t.chujSurface(), w)))
            return false;
        if (!spec.tokenStartsWith().isEmpty() && spec.tokenStartsWith().stream().noneMatch(w -> norm(t.chujSurface()).startsWith(norm(w))))
            return false;
        if (!spec.tokenEndsWith().isEmpty() && spec.tokenEndsWith().stream().noneMatch(w -> norm(t.chujSurface()).endsWith(norm(w))))
            return false;
        if (!spec.tokenHasSegment().isEmpty() && spec.tokenHasSegment().stream().noneMatch(w -> containsIgnoreCase(t.chujSegments(), w)))
            return false;
        if (spec.tokenStartsWithVowel() && !startsWithVowel(t.chujSurface())) return false;
        if (spec.betweenLength() != null && spec.betweenLength() != 1) return false;
        if (!spec.tokenSequences().isEmpty() && spec.tokenSequences().stream().noneMatch(seq -> sequenceMatches(tokens, i, seq)))
            return false;
        return true;
    }

    public static boolean sequenceMatches(List<AlignedToken> tokens, int i, List<String> seq) {
        if (seq.isEmpty()) return true;
        if (i + seq.size() > tokens.size()) return false;
        for (int k = 0; k < seq.size(); k++) {
            String expected = seq.get(k);
            if ("_".equals(expected)) continue;
            if (!eq(tokens.get(i + k).chujSurface(), expected)) return false;
        }
        return true;
    }

    private static boolean startsWithVowel(String s) {
        if (s == null || s.isBlank()) return false;
        char c = Character.toLowerCase(s.charAt(0));
        return "aeiou".indexOf(c) >= 0;
    }

    private static boolean containsIgnoreCase(List<String> values, String needle) {
        return values.stream().anyMatch(v -> eq(v, needle));
    }

    private static boolean eq(String a, String b) {
        return norm(a).equals(norm(b));
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
