package org.titiplex.backend.dto;

import java.util.UUID;

public record EntryDetailDto(
        UUID id,
        int documentOrder,
        String contextText,
        String surfaceText,
        String rawChujText,
        String rawGlossText,
        String translation,
        String comments,
        String correctedChujText,
        String correctedGlossText,
        String correctedTranslation,
        boolean approved,
        String conlluPreview
) {
}