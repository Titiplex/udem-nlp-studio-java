package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class DeletePartsRule implements CorrectionRule {
    private final String id;
    private final Set<String> parts;

    public DeletePartsRule(String id, List<String> parts) {
        this.id = id;
        this.parts = Set.copyOf(parts);
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
                String ch = tok.chujSegments().get(idx);
                if (parts.contains(ch) && tok.chujSegments().size() > 1) continue;
                newChuj.add(ch);
                if (idx < tok.glossSegments().size()) newGloss.add(tok.glossSegments().get(idx));
            }
            context.replace(i, context.rebuildToken(newChuj, newGloss));
            if (newChuj.isEmpty() && !newGloss.isEmpty()) context.shiftRightGloss(i);
        }
    }
}
