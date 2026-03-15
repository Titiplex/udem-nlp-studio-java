package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

public final class SimpleLemmatizer {
    public String lemmaFor(AlignedToken token, String upos) {
        if ("PUNCT".equals(upos)) return token.chujSurface();
        if (token.chujSegments().isEmpty()) return "_";
        return token.chujSegments().getFirst();
    }
}
