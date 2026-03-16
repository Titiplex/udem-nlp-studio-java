package org.titiplex.conllu;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SpanishLexicon {
    private static final Set<String> COMMON_VERBS = new LinkedHashSet<>(List.of(
            "ser", "estar", "ir", "venir", "hacer", "decir", "dar", "ver", "tener", "poder",
            "querer", "regresar", "existir", "salir", "entrar", "comer", "beber", "vivir"
    ));

    private SpanishLexicon() {
    }

    static boolean isSpanishVerb(String value) {
        if (value == null || value.isBlank()) return false;
        String v = value.toLowerCase(Locale.ROOT);
        return COMMON_VERBS.contains(v) || v.matches(".*(ar|er|ir)$");
    }
}
