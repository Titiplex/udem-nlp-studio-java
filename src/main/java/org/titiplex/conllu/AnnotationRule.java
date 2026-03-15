package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class AnnotationRule {
    private final Pattern regex;
    private final Set<String> inList;
    private final boolean onGloss;
    private final String upos;
    private final Map<String, String> feats;

    public AnnotationRule(Pattern regex, Set<String> inList, boolean onGloss, String upos, Map<String, String> feats) {
        this.regex = regex;
        this.inList = inList == null ? Set.of() : Set.copyOf(inList);
        this.onGloss = onGloss;
        this.upos = upos;
        this.feats = feats == null ? Map.of() : Map.copyOf(feats);
    }

    public boolean matches(AlignedToken tok) {
        String surface = onGloss ? tok.glossSurface() : tok.chujSurface();
        if (regex != null && surface != null && regex.matcher(surface).find()) return true;
        if (!inList.isEmpty())
            for (String s : (onGloss ? tok.glossSegments() : tok.chujSegments())) if (inList.contains(s)) return true;
        return regex == null && inList.isEmpty();
    }

    public void apply(ConlluLine line) {
        if (upos != null && !upos.isBlank() && "_".equals(line.upos())) line.setUpos(upos);
        line.putAllFeats(feats, false);
    }
}
