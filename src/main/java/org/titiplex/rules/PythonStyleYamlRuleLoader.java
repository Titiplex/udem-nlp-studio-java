package org.titiplex.rules;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class PythonStyleYamlRuleLoader {
    @SuppressWarnings("unchecked")
    public List<CorrectionRule> load(InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        if (data == null) {
            return List.of();
        }

        List<Map<String, Object>> rawRules = (List<Map<String, Object>>) data.getOrDefault("rules", List.of());
        List<CorrectionRule> out = new ArrayList<>();
        for (Map<String, Object> rawRule : rawRules) {
            String id = RuleYamlSupport.string(rawRule.get("id"), RuleYamlSupport.string(rawRule.get("name"), "unnamed_rule"));
            Map<String, Object> rewrite = RuleYamlSupport.map(rawRule.get("rewrite"));
            MatchSpec spec = MatchParser.parse(rawRule, rewrite);

            // Deletions should happen early regardless of merge/rewrite ordering.
            loadDeleteRules(id, rewrite, out);

            for (Map.Entry<String, Object> entry : rawRule.entrySet()) {
                String key = entry.getKey();
                switch (key) {
                    case "merge" -> loadMergeRules(id, RuleYamlSupport.map(entry.getValue()), out);
                    case "rewrite" -> loadRewriteRules(id, rewrite, spec, out);
                    default -> {
                    }
                }
            }
        }
        return out;
    }

    private static void loadRewriteRules(String id, Map<String, Object> rewrite, MatchSpec spec, List<CorrectionRule> out) {
        loadBeforeAfterRules(id, rewrite, spec, out);
        loadInsertRules(id, rewrite, spec, out);
        loadRegexRules(id, rewrite, spec, out);
        loadSplitRules(id, rewrite, spec, out);
    }

    private static void loadDeleteRules(String id, Map<String, Object> rewrite, List<CorrectionRule> out) {
        Map<String, Object> delete = RuleYamlSupport.map(rewrite.get("delete"));
        if (delete.isEmpty()) {
            return;
        }
        String type = RuleYamlSupport.string(delete.get("type"), "");
        List<String> chars = RuleYamlSupport.stringList(delete.get("chars"));
        if ("chars".equals(type)) {
            out.add(new DeleteCharsRule(id + ":delete_chars", chars));
        } else if ("part".equals(type)) {
            out.add(new DeletePartsRule(id + ":delete_parts", chars));
        }
    }

    private static void loadBeforeAfterRules(String id, Map<String, Object> rewrite, MatchSpec spec, List<CorrectionRule> out) {
        List<String> before = RuleYamlSupport.stringList(rewrite.get("before"));
        List<String> after = RuleYamlSupport.stringList(rewrite.get("after"));

        Map<String, Object> gloss = RuleYamlSupport.map(rewrite.get("gloss"));
        List<String> glossBefore = RuleYamlSupport.stringList(gloss.get("before"));
        List<String> glossAfter = RuleYamlSupport.stringList(gloss.get("after"));

        Map<String, String> surfaceMap = buildMapping(before, after);
        Map<String, String> glossMap = buildMapping(glossBefore, glossAfter);
        if (!surfaceMap.isEmpty() || !glossMap.isEmpty()) {
            out.add(new SurfaceRewriteRule(id + ":rewrite", spec, surfaceMap, glossMap));
        }
    }

    private static void loadInsertRules(String id, Map<String, Object> rewrite, MatchSpec spec, List<CorrectionRule> out) {
        Map<String, Object> ins = RuleYamlSupport.map(rewrite.get("insert"));
        if (ins.isEmpty()) {
            return;
        }
        out.add(new InsertSegmentRule(
                id + ":insert",
                spec,
                RuleYamlSupport.string(ins.get("segment"), ""),
                RuleYamlSupport.intValue(ins.get("token"), 1),
                RuleYamlSupport.intValue(ins.get("position"), 1)
        ));
    }

    private static void loadMergeRules(String id, Map<String, Object> merge, List<CorrectionRule> out) {
        if (merge.isEmpty()) {
            return;
        }
        Map<String, Object> match = RuleYamlSupport.map(merge.get("match"));
        Object tokens = match.get("tokens");
        List<List<String>> seqs = new ArrayList<>();
        if (tokens instanceof List<?> l && !l.isEmpty() && l.getFirst() instanceof List) {
            for (Object o : l) {
                seqs.add(RuleYamlSupport.stringList(o));
            }
        } else if (tokens != null) {
            seqs.add(RuleYamlSupport.stringList(tokens));
        }
        if (!seqs.isEmpty()) {
            out.add(new MergeSequenceRule(id + ":merge", seqs));
        }
    }

    private static void loadRegexRules(String id, Map<String, Object> rewrite, MatchSpec spec, List<CorrectionRule> out) {
        Map<String, Object> regexSub = RuleYamlSupport.map(rewrite.get("regex_sub"));
        if (regexSub.isEmpty()) {
            return;
        }
        boolean ignoreCase = RuleYamlSupport.bool(regexSub.get("ignore_case"), false);
        int flags = ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
        Pattern pattern = Pattern.compile(RuleYamlSupport.string(regexSub.get("pattern"), ""), flags);
        String scope = RuleYamlSupport.string(regexSub.get("scope"), "chuj");
        String repl = RuleYamlSupport.string(regexSub.get("repl"), "");

        if ("gloss".equalsIgnoreCase(scope)) {
            out.add(new RegexSubRule(id + ":regex_gloss", spec, scope, pattern, repl));
        } else {
            out.add(new RegexSubRule(id + ":regex_sub", spec, scope, pattern, repl));
        }
    }

    private static void loadSplitRules(String id, Map<String, Object> rewrite, MatchSpec spec, List<CorrectionRule> out) {
        Map<String, Object> split = RuleYamlSupport.map(rewrite.get("split"));
        if (!split.isEmpty() && "end".equals(RuleYamlSupport.string(split.get("type"), ""))) {
            out.add(new SplitDirectionalRule(id + ":split_end", spec, RuleYamlSupport.stringList(split.get("tokens"))));
        }
    }

    private static Map<String, String> buildMapping(List<String> before, List<String> after) {
        if (before.isEmpty() || after.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        if (after.size() == 1) {
            String target = after.getFirst();
            for (String b : before) {
                out.put(norm(b), target);
            }
            return out;
        }
        for (int i = 0; i < Math.min(before.size(), after.size()); i++) {
            out.put(norm(before.get(i)), after.get(i));
        }
        return out;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
