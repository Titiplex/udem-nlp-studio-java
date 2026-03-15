package org.titiplex.rules;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            if (rewrite.isEmpty()) continue;
            Map<String, Object> delete = RuleYamlSupport.map(rewrite.get("delete"));
            if (!delete.isEmpty()) {
                String type = RuleYamlSupport.string(delete.get("type"), "");
                List<String> chars = RuleYamlSupport.stringList(delete.get("chars"));
                if ("chars".equals(type)) out.add(new DeleteCharsRule(id + ":delete_chars", chars));
                else if ("part".equals(type)) out.add(new DeletePartsRule(id + ":delete_parts", chars));
            }
            Map<String, Object> gloss = RuleYamlSupport.map(rewrite.get("gloss"));
            if (!gloss.isEmpty() && gloss.containsKey("before") && gloss.containsKey("after")) {
                boolean ignoreCase = RuleYamlSupport.bool(gloss.get("ignore_case"), true);
                List<String> before = RuleYamlSupport.stringList(gloss.get("before"));
                List<String> after = RuleYamlSupport.stringList(gloss.get("after"));
                RuleSelector selector = new RuleSelector(true, null, Set.copyOf(before), before, ignoreCase);
                out.add(new GlossBeforeAfterRule(id + ":gloss_before_after", selector, GlossBeforeAfterRule.buildMapping(before, after, ignoreCase), ignoreCase));
            }
            Map<String, Object> replaceGloss = RuleYamlSupport.map(rewrite.get("replace_gloss"));
            if (!replaceGloss.isEmpty()) {
                out.add(new GlossReplaceRule(id + ":replace_gloss", RuleYamlSupport.string(replaceGloss.get("regex"), ""), RuleYamlSupport.string(replaceGloss.get("replacement"), ""), RuleYamlSupport.bool(replaceGloss.get("ignore_case"), false)));
            }
            Map<String, Object> regexSub = RuleYamlSupport.map(rewrite.get("regex_sub"));
            if (!regexSub.isEmpty()) {
                boolean ignoreCase = RuleYamlSupport.bool(regexSub.get("ignore_case"), false);
                int flags = ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
                Pattern pattern = Pattern.compile(RuleYamlSupport.string(regexSub.get("pattern"), ""), flags);
                out.add(new RegexSubRule(id + ":regex_sub", new RuleSelector(false, null, Set.of(), List.of(), false), RuleYamlSupport.string(regexSub.get("scope"), "chuj"), pattern, RuleYamlSupport.string(regexSub.get("repl"), "")));
            }
        }
        return out;
    }
}
