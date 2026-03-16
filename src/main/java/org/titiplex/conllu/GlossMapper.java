package org.titiplex.conllu;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class GlossMapper {
    private final Map<String, String> posMap = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> featMap = new LinkedHashMap<>();

    public GlossMapper() {
        putPos("adv", "ADV");
        putPos("adj", "ADJ");
        putPos("noun", "NOUN");
        putPos("verb", "VERB");
        putPos("prep", "ADP");
        putPos("pron", "PRON");
        putPos("punct", "PUNCT");
        putPos("num", "NUM");
        putFeat("sg", Map.of("Number", "Sing"));
        putFeat("pl", Map.of("Number", "Plur"));
        putFeat("pfv", Map.of("Aspect", "Perf"));
        putFeat("ipfv", Map.of("Aspect", "Imp"));
        putFeat("prog", Map.of("Aspect", "Prog"));
        putFeat("neg", Map.of("Polarity", "Neg"));
        putFeat("prx", Map.of("Deixis", "Prox"));
        putFeat("prox", Map.of("Deixis", "Prox"));
        putFeat("a1", Map.of("Person", "1", "Number", "Sing", "Poss", "Yes"));
        putFeat("a2", Map.of("Person", "2", "Number", "Sing", "Poss", "Yes"));
        putFeat("a3", Map.of("Person", "3", "Poss", "Yes"));
        putFeat("b1", Map.of("Person", "1", "Number", "Sing"));
        putFeat("b2", Map.of("Person", "2", "Number", "Sing"));
        putFeat("b3", Map.of("Person", "3"));
    }

    public void putPos(String gloss, String upos) {
        if (gloss != null && upos != null) posMap.put(gloss.toLowerCase(Locale.ROOT), upos);
    }

    public void putFeat(String gloss, Map<String, String> feats) {
        if (gloss != null && feats != null && !feats.isEmpty())
            featMap.put(gloss.toLowerCase(Locale.ROOT), Map.copyOf(feats));
    }

    public String resolvePos(String gloss) {
        return gloss == null ? null : posMap.get(gloss.toLowerCase(Locale.ROOT));
    }

    public Map<String, String> resolveFeats(String gloss) {
        return gloss == null ? Map.of() : featMap.getOrDefault(gloss.toLowerCase(Locale.ROOT), Map.of());
    }
}
