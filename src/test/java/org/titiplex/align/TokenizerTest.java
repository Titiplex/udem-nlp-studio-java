package org.titiplex.align;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenizerTest {
    @Test
    void tokenizeLineDropsLeadingId() {
        assertEquals(List.of("ha", "tin"), Tokenizer.tokenizeLine("1 ha tin", true));
    }

    @Test
    void tokenizeWordSplitsOnHyphen() {
        assertEquals(List.of("ek'", "nak", "in"), Tokenizer.tokenizeWord("ek'-nak-in"));
    }

    @Test
    void punctuationDetectionWorks() {
        assertTrue(Tokenizer.isPunctuation("."));
    }
}
