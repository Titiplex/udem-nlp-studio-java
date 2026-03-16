package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;

public final class DeleteCharsRule implements CorrectionRule {
    private final String id;
    private final List<String> chars;

    public DeleteCharsRule(String id, List<String> chars) {
        this.id = id;
        this.chars = List.copyOf(chars);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void apply(RuleContext context) {
        for (int i = 0; i < context.size(); i++) {
            AlignedToken tok = context.get(i);
            if (tok.chujSegments().isEmpty()) continue;
            List<String> newChuj = new ArrayList<>();
            List<String> newGloss = new ArrayList<>();
            for (int idx = 0; idx < tok.chujSegments().size(); idx++) {
                String cleaned = tok.chujSegments().get(idx);
                for (String c : chars) cleaned = cleaned.replace(c, "");
                if (cleaned.isEmpty()) continue;
                newChuj.add(cleaned);
                if (idx < tok.glossSegments().size()) newGloss.add(tok.glossSegments().get(idx));
            }
            if (newGloss.isEmpty()) newGloss = new ArrayList<>(tok.glossSegments());
            context.replace(i, context.rebuildToken(newChuj, newGloss));
            if (newChuj.isEmpty() && !newGloss.isEmpty()) context.shiftRightGloss(i);
        }
    }
}
