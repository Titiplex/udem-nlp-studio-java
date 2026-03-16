package org.titiplex.stats;

import org.titiplex.conllu.SpanishLexicon;
import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CorpusStats {
    private int singleMorpheme = 0;
    private int multipleMorphemes = 0;
    private int spanishRootMultipleMorphemes = 0;
    private final Set<String> distinctTokens = new LinkedHashSet<>();
    private final Set<String> distinctMorphemes = new LinkedHashSet<>();

    public void accept(CorrectedBlock block) {
        for (AlignedToken token : block.alignedTokens()) {
            String joined = token.chujSurface();
            if (!joined.isBlank()) distinctTokens.add(joined);
            for (String seg : token.chujSegments()) {
                if (!seg.isBlank()) distinctMorphemes.add(seg);
            }
            if (token.chujSegments().size() <= 1) {
                singleMorpheme++;
            } else {
                multipleMorphemes++;
                if (hasSpanishVerbGloss(token.glossSegments())) {
                    spanishRootMultipleMorphemes++;
                }
            }
        }
    }

    private boolean hasSpanishVerbGloss(List<String> glossSegments) {
        for (String gloss : glossSegments) {
            if (SpanishLexicon.isSpanishVerb(gloss)) return true;
        }
        return false;
    }

    public String toReportString() {
        int total = singleMorpheme + multipleMorphemes;
        StringBuilder sb = new StringBuilder();
        sb.append("Number of different tokens: ").append(distinctTokens.size()).append('\n');
        sb.append("Number of different morphemes: ").append(distinctMorphemes.size()).append('\n');
        sb.append("Total number of single morpheme words: ").append(singleMorpheme).append('\n');
        sb.append("Total number of multiple morpheme words: ").append(multipleMorphemes).append('\n');
        sb.append("Number of spanish root words among multiple morphemes: ").append(spanishRootMultipleMorphemes).append('\n');
        if (total > 0) {
            sb.append(String.format("Percentage of multiple morphemes: %.2f%%%n", 100.0 * multipleMorphemes / total));
            sb.append(String.format("Percentage of single morphemes: %.2f%%%n", 100.0 * singleMorpheme / total));
            sb.append(String.format("Percentage of spanish roots (total tokens): %.2f%%%n", 100.0 * spanishRootMultipleMorphemes / total));
        }
        if (multipleMorphemes > 0) {
            sb.append(String.format("Percentage of spanish roots (multiple morphemes): %.2f%%%n",
                    100.0 * spanishRootMultipleMorphemes / multipleMorphemes));
        }
        sb.append("Tokens:\n").append(String.join(", ", distinctTokens)).append('\n');
        return sb.toString();
    }
}
