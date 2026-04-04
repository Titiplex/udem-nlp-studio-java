package org.titiplex.backend.dto;

import java.util.UUID;

public record EntryDetailDto(
        UUID id,
        int documentOrder,
        String rawChujText,
        String rawGlossText,
        String translation,
        String correctedChujText,
        String correctedGlossText,
        String correctedTranslation,
        boolean approved,
        String conlluPreview
) {
}