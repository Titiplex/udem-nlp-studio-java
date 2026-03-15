package org.titiplex.conllu;

import org.titiplex.rules.RuleYamlSupport;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                config.glossMapper().putFeat(entry.getKey(), castStringMap(RuleYamlSupport.map(entry.getValue())));
            }
        }

        List<Map<String, Object>> rules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());
        for (Map<String, Object> rawRule : rules) {
            Map<String, Object> match = RuleYamlSupport.map(rawRule.get("match"));
            boolean onGloss = match.containsKey("gloss");
            Map<String, Object> effective = onGloss ? RuleYamlSupport.map(match.get("gloss")) : match;
            String regexText = RuleYamlSupport.string(effective.get("regex"), "");
            Pattern regex = regexText.isBlank() ? null : Pattern.compile(regexText);
            Set<String> inList = Set.copyOf(RuleYamlSupport.stringList(effective.get("in_list")));
            Map<String, Object> set = RuleYamlSupport.map(rawRule.get("set"));
            config.rules().add(new AnnotationRule(regex, inList, onGloss,
                    RuleYamlSupport.string(set.get("upos"), ""),
                    castStringMap(RuleYamlSupport.map(set.get("feats")))));
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
