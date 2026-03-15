package org.titiplex.rules;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public final class PythonStyleYamlRuleLoader {
    @SuppressWarnings("unchecked")
    public List<CorrectionRule> load(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        List<Map<String, Object>> rawRules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());
        List<CorrectionRule> out = new ArrayList<>();

        for (Map<String, Object> rawRule : rawRules) {
            String id = RuleYamlSupport.string(rawRule.get("id"), RuleYamlSupport.string(rawRule.get("name"), "unnamed_rule"));
            Map<String, Object> rewrite = RuleYamlSupport.map(rawRule.get("rewrite"));
            Map<String, Object> merge = RuleYamlSupport.map(rawRule.get("merge"));
            MatchSpec spec = MatchParser.parse(rawRule, rewrite);

            Map<String, Object> delete = RuleYamlSupport.map(rewrite.get("delete"));
            if (!delete.isEmpty()) {
                String type = RuleYamlSupport.string(delete.get("type"), "");
                List<String> chars = RuleYamlSupport.stringList(delete.get("chars"));
                if ("chars".equals(type)) out.add(new DeleteCharsRule(id + ":delete_chars", chars));
                else if ("part".equals(type)) out.add(new DeletePartsRule(id + ":delete_parts", chars));
            }

            Map<String, Object> gloss = RuleYamlSupport.map(rewrite.get("gloss"));
            List<String> before = RuleYamlSupport.stringList(rewrite.get("before"));
            List<String> after = RuleYamlSupport.stringList(rewrite.get("after"));
            Map<String, String> surfaceMap = buildMapping(before, after);
            Map<String, String> glossMap = gloss.containsKey("before") && gloss.containsKey("after")
                    ? buildMapping(RuleYamlSupport.stringList(gloss.get("before")), RuleYamlSupport.stringList(gloss.get("after")))
                    : Map.of();
            if (!surfaceMap.isEmpty() || !glossMap.isEmpty()) {
                out.add(new SurfaceRewriteRule(id + ":surface_rewrite", spec, surfaceMap, glossMap));
            }

            if (rewrite.containsKey("insert")) {
                Map<String, Object> ins = RuleYamlSupport.map(rewrite.get("insert"));
                out.add(new InsertSegmentRule(id + ":insert", spec, RuleYamlSupport.string(ins.get("segment"), ""), RuleYamlSupport.intValue(ins.get("token"), 1), RuleYamlSupport.intValue(ins.get("position"), 1)));
            }

            if (!merge.isEmpty()) {
                Map<String, Object> match = RuleYamlSupport.map(merge.get("match"));
                Object tokens = match.get("tokens");
                List<List<String>> seqs = new ArrayList<>();
                if (tokens instanceof List<?> l && !l.isEmpty() && l.get(0) instanceof List<?>) {
                    for (Object o : l) seqs.add(RuleYamlSupport.stringList(o));
                } else if (tokens != null) {
                    seqs.add(RuleYamlSupport.stringList(tokens));
                }
                if (!seqs.isEmpty()) out.add(new MergeSequenceRule(id + ":merge", seqs));
            }

            Map<String, Object> regexSub = RuleYamlSupport.map(rewrite.get("regex_sub"));
            if (!regexSub.isEmpty()) {
                boolean ignoreCase = RuleYamlSupport.bool(regexSub.get("ignore_case"), false);
                int flags = ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
                Pattern pattern = Pattern.compile(RuleYamlSupport.string(regexSub.get("pattern"), ""), flags);
                out.add(new RegexSubRule(id + ":regex_sub", new RuleSelector(false, null, Set.of(), List.of(), false), RuleYamlSupport.string(regexSub.get("scope"), "chuj"), pattern, RuleYamlSupport.string(regexSub.get("repl"), "")));
            }

            Map<String, Object> split = RuleYamlSupport.map(rewrite.get("split"));
            if (!split.isEmpty() && "end".equals(RuleYamlSupport.string(split.get("type"), ""))) {
                out.add(new SplitDirectionalRule(id + ":split_end", RuleYamlSupport.stringList(split.get("tokens"))));
            }
        }
        return out;
    }

    private static Map<String, String> buildMapping(List<String> before, List<String> after) {
        if (before.isEmpty() || after.isEmpty()) return Map.of();
        Map<String, String> out = new HashMap<>();
        if (after.size() == 1) {
            for (String b : before) out.put(norm(b), after.getFirst());
        } else {
            for (int i = 0; i < Math.min(before.size(), after.size()); i++) out.put(norm(before.get(i)), after.get(i));
        }
        return out;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
