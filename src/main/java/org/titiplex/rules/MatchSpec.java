package org.titiplex.rules;

import java.util.List;

public record MatchSpec(
        List<List<String>> tokenSequences,
        List<String> tokenIsWord,
        List<String> tokenAny,
        List<String> tokenStartsWith,
        List<String> tokenEndsWith,
        List<String> tokenHasSegment,
        boolean tokenStartsWithVowel,
        List<String> glossValues,
        List<String> glossStartsWith,
        String glossSpecial,
        Integer betweenLength,
        String targets,
        String chujSide,
        String rootFromGloss,
        boolean rootStartsWithVowel
) {
    public static MatchSpec empty() {
        return new MatchSpec(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), false, List.of(), List.of(), null, null, null, null, null, false);
    }
}
