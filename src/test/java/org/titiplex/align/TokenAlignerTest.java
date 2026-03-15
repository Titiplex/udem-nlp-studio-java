package org.titiplex.align;

import org.junit.jupiter.api.Test;
import org.titiplex.model.AlignedToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class TokenAlignerTest {
    @Test
    void alignerKeepsSimplePairs() {
        TokenAligner aligner = new TokenAligner();
        List<AlignedToken> aligned = aligner.align(
                List.of("ha", "hin", "tik"),
                List.of("APV", "B1", "PRX")
        );

        assertEquals(3, aligned.size());
        assertEquals("ha", aligned.get(0).chujSurface());
        assertEquals("APV", aligned.get(0).glossSurface());
    }
}
