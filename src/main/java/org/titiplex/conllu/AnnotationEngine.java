package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;
import org.titiplex.model.CorrectedBlock;

import java.util.List;

public final class AnnotationEngine {
    private final AnnotationConfig config;
    private final SimpleLemmatizer lemmatizer = new SimpleLemmatizer();

    public AnnotationEngine(AnnotationConfig config) {
        this.config = config;
    }

    public ConlluEntry annotate(CorrectedBlock block) {
        ConlluEntry entry = new ConlluEntry();
        entry.headers().addAll(ConlluEntry.makeHeaders(Integer.toString(block.id()), block.chujText(), block.translation(), block.glossText()));
        int id = 1;
        for (AlignedToken token : block.alignedTokens()) {
            if (token.chujSurface().isBlank() && token.glossSurface().isBlank()) continue;
            ConlluLine line = ConlluLine.basic(Integer.toString(id++), token.chujSurface().isBlank() ? token.glossSurface() : token.chujSurface());
            line.setMisc(token.glossSurface().isBlank() ? "_" : "Gloss=" + token.glossSurface());
            for (String gloss : token.glossSegments()) {
                String pos = config.glossMapper().resolvePos(gloss);
                if (pos != null && "_".equals(line.upos())) line.setUpos(pos);
                line.putAllFeats(config.glossMapper().resolveFeats(gloss), false);
            }
            for (AnnotationRule rule : config.rules()) if (rule.matches(token)) rule.apply(line);
            if ("_".equals(line.upos())) line.setUpos(guessUpos(token));
            line.setLemma(lemmatizer.lemmaFor(token, line.upos()));
            if ("PUNCT".equals(line.upos())) line.setDeprel("punct");
            entry.lines().add(line);
        }
        return entry;
    }

    private String guessUpos(AlignedToken token) {
        String form = token.chujSurface().isBlank() ? token.glossSurface() : token.chujSurface();
        if (form == null || form.isBlank()) return "_";
        if (form.chars().allMatch(Character::isDigit)) return "NUM";
        if (form.length() == 1 && ".,;:!?()[]{}".contains(form)) return "PUNCT";
        for (String gloss : token.glossSegments()) {
            String lower = gloss.toLowerCase();
            if (lower.matches("a[123].*|b[123].*")) return "PRON";
            if (List.of("adv", "adj", "noun", "verb", "prep", "pron", "punct", "num").contains(lower)) {
                String pos = config.glossMapper().resolvePos(lower);
                if (pos != null) return pos;
            }
        }
        return "_";
    }
}
