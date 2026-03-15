package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;

public final class RuleContext {
    private final List<AlignedToken> alignedTokens;

    public RuleContext(List<AlignedToken> alignedTokens) {
        this.alignedTokens = new ArrayList<>(alignedTokens);
    }

    public List<AlignedToken> alignedTokens() {
        return List.copyOf(alignedTokens);
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
}
