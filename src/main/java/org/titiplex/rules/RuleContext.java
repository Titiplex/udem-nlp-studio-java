package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RuleContext {
    private final List<AlignedToken> alignedTokens;

    public RuleContext(List<AlignedToken> alignedTokens) {
        this.alignedTokens = new ArrayList<>(alignedTokens);
    }

    public List<AlignedToken> alignedTokens() {
        return List.copyOf(alignedTokens);
    }

    List<AlignedToken> alignedTokensMutable() {
        return alignedTokens;
    }

    public int size() {
        return alignedTokens.size();
    }

    public AlignedToken get(int index) {
        return alignedTokens.get(index);
    }

    public void replace(int index, AlignedToken token) {
        alignedTokens.set(index, token);
    }

    public void removeAt(int index) {
        alignedTokens.remove(index);
    }

    public AlignedToken rebuildToken(List<String> chujSegments, List<String> glossSegments) {
        return AlignedToken.of(String.join("-", chujSegments), String.join("-", glossSegments), chujSegments, glossSegments);
    }

    public void shiftRightGloss(int shiftIndex) {
        if (shiftIndex < 0 || shiftIndex >= alignedTokens.size()) return;
        AlignedToken current = alignedTokens.get(shiftIndex);
        if (!current.chujSegments().isEmpty()) return;
        for (int k = shiftIndex + 1; k < alignedTokens.size(); k++) {
            AlignedToken next = alignedTokens.get(k);
            if (!next.chujSegments().isEmpty()) {
                alignedTokens.set(shiftIndex, AlignedToken.of(next.chujSurface(), current.glossSurface(), next.chujSegments(), current.glossSegments()));
                alignedTokens.set(k, AlignedToken.of("", next.glossSurface(), List.of(), next.glossSegments()));
                return;
            }
        }
    }

    public int targetIndex(int anchor, String targets) {
        if (targets == null || targets.isBlank()) return anchor;
        return switch (targets.toLowerCase(Locale.ROOT)) {
            case "i" -> anchor;
            case "j" -> Math.min(anchor + 1, alignedTokens.size() - 1);
            default -> anchor;
        };
    }

    public boolean startsWithVowelAt(int index) {
        if (index < 0 || index >= alignedTokens.size()) return false;
        String s = alignedTokens.get(index).chujSurface();
        if (s == null || s.isBlank()) return false;
        char c = Character.toLowerCase(s.charAt(0));
        return "aeiou".indexOf(c) >= 0;
    }

    public boolean hasSpanishVerbAt(int index) {
        if (index < 0 || index >= alignedTokens.size()) return false;
        AlignedToken t = alignedTokens.get(index);
        for (String g : t.glossSegments()) {
            if (looksLikeSpanishVerb(g)) return true;
        }
        return false;
    }

    public String inferredRootAt(int anchor, String side) {
        int idx = switch ((side == null ? "" : side).toLowerCase(Locale.ROOT)) {
            case "j" -> Math.min(anchor + 1, alignedTokens.size() - 1);
            case "i" -> anchor;
            default -> anchor;
        };
        if (idx < 0 || idx >= alignedTokens.size()) return "";
        return alignedTokens.get(idx).chujSurface();
    }

    private boolean looksLikeSpanishVerb(String gloss) {
        if (gloss == null || gloss.isBlank()) return false;
        String g = gloss.trim();
        String lower = g.toLowerCase(Locale.ROOT);
        if (lower.contains(" ")) return false;
        if (g.matches(".*[A-Z].*")) return false;
        return lower.matches(".*(ar|er|ir)$") || List.of(
                "estar", "ser", "ir", "venir", "hacer", "decir", "dar", "ver", "poder", "tener",
                "regresar", "existir", "exist", "malo", "feo", "bueno", "hasta", "todavia"
        ).contains(lower);
    }
}
