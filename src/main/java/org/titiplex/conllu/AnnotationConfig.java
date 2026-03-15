package org.titiplex.conllu;

import org.titiplex.model.AlignedToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnnotationConfig {
    private final GlossMapper glossMapper = new GlossMapper();
    private final List<AnnotationRule> rules = new ArrayList<>();
    private final Map<String, ExtractorDef> extractors = new LinkedHashMap<>();

    public GlossMapper glossMapper() {
        return glossMapper;
    }

    public List<AnnotationRule> rules() {
        return rules;
    }

    public Map<String, ExtractorDef> extractors() {
        return extractors;
    }

    public void applyExtractor(String name, AlignedToken token, ConlluLine line) {
        ExtractorDef ex = extractors.get(name);
        if (ex == null) return;
        Map<String, String> a = null;
        Map<String, String> b = null;
        Pattern pattern = Pattern.compile("^([AB])([123])(PL)?$", Pattern.CASE_INSENSITIVE);
        for (String gloss : token.glossSegments()) {
            Matcher m = pattern.matcher(gloss.toUpperCase(Locale.ROOT));
            if (!m.matches()) continue;
            Map<String, String> values = new LinkedHashMap<>();
            values.put("person", m.group(2));
            values.put("number", m.group(3) != null ? "Plur" : "Sing");
            if ("A".equals(m.group(1))) a = values;
            else b = values;
        }
        if (a != null && b != null) {
            setAgreement(line, a, b, "Trans");
        } else if (b != null) {
            line.putFeat("Pers[subj]", b.get("person"), false);
            line.putFeat("Number[subj]", b.get("number"), false);
            line.putFeat("SubCat", "Intrans", false);
        } else if (a != null) {
            line.putFeat("Pers[subj]", a.get("person"), false);
            line.putFeat("Number[subj]", a.get("number"), false);
            line.putFeat("Pers[obj]", "3", false);
            line.putFeat("Number[obj]", "Sing", false);
            line.putFeat("SubCat", "Trans", false);
        }
        if (a != null || b != null) line.setUpos("VERB");
    }

    private void setAgreement(ConlluLine line, Map<String, String> a, Map<String, String> b, String subCat) {
        line.putFeat("Pers[subj]", a.get("person"), false);
        line.putFeat("Number[subj]", a.get("number"), false);
        line.putFeat("Pers[obj]", b.get("person"), false);
        line.putFeat("Number[obj]", b.get("number"), false);
        line.putFeat("SubCat", subCat, false);
    }

    public record ExtractorDef(String name) {
    }
}
