package org.titiplex.conllu;

import org.titiplex.rules.RuleYamlSupport;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public final class AnnotationConfigLoader {
    @SuppressWarnings("unchecked")
    public AnnotationConfig load(InputStream inputStream) {
        AnnotationConfig config = new AnnotationConfig();
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        if (data == null) return config;

        Map<String, Object> glossMap = RuleYamlSupport.map(data.get("gloss_map"));
        for (Map<String, Object> pos : RuleYamlSupport.mapList(glossMap.get("pos"))) {
            for (var entry : pos.entrySet()) {
                config.glossMapper().putPos(entry.getKey(), entry.getValue().toString());
            }
        }
        for (Map<String, Object> feat : RuleYamlSupport.mapList(glossMap.get("feats"))) {
            for (var entry : feat.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof List<?> pair && pair.size() >= 2) {
                    config.glossMapper().putFeat(entry.getKey(), Map.of(pair.get(0).toString(), pair.get(1).toString()));
                } else {
                    config.glossMapper().putFeat(entry.getKey(), castStringMap(RuleYamlSupport.map(value)));
                }
            }
        }

        Map<String, Object> extractors = RuleYamlSupport.map(data.get("extractors"));
        for (String name : extractors.keySet()) {
            config.extractors().put(name, new AnnotationConfig.ExtractorDef(name));
        }

        List<Map<String, Object>> rules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());
        for (Map<String, Object> rawRule : rules) {
            Map<String, Object> match = RuleYamlSupport.map(rawRule.get("match"));
            boolean onGloss = match.containsKey("gloss");
            String glossSpecial = null;
            Map<String, Object> effective = onGloss ? RuleYamlSupport.map(match.get("gloss")) : match;
            Object glossObj = match.get("gloss");
            if (glossObj instanceof Map<?, ?> gm) effective = (Map<String, Object>) gm;
            if (glossObj instanceof String gs && "spanish_verbs".equalsIgnoreCase(gs)) {
                glossSpecial = gs;
                effective = Map.of();
                onGloss = true;
            }
            String regexText = RuleYamlSupport.string(effective.get("regex"), "");
            Pattern regex = regexText.isBlank() ? null : Pattern.compile(regexText);
            Set<String> inList = new LinkedHashSet<>(RuleYamlSupport.stringList(effective.get("in_list")));
            if (glossObj instanceof List<?>) inList.addAll(RuleYamlSupport.stringList(glossObj));
            Map<String, Object> set = RuleYamlSupport.map(rawRule.get("set"));
            List<Map<String, String>> extractsList = new ArrayList<>();
            Object extractObj = set.get("extract");
            if (extractObj instanceof List<?> l) {
                for (Object o : l) extractsList.add(castStringMap(RuleYamlSupport.map(o)));
            }
            config.rules().add(new AnnotationRule(
                    RuleYamlSupport.string(rawRule.get("name"), ""),
                    RuleYamlSupport.string(rawRule.get("scope"), "token"),
                    regex,
                    inList,
                    onGloss,
                    glossSpecial,
                    RuleYamlSupport.string(set.get("upos"), ""),
                    castStringMap(RuleYamlSupport.map(set.get("feats"))),
                    extractsList
            ));
        }
        return config;
    }

    private Map<String, String> castStringMap(Map<String, Object> raw) {
        Map<String, String> out = new LinkedHashMap<>();
        for (var e : raw.entrySet()) {
            if (e.getValue() != null) {
                out.put(e.getKey(), e.getValue().toString());
            }
        }
        return out;
    }
}
