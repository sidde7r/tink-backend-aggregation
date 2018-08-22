package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecipientAccountEntityTest {

    @Test
    public void testRecipientPatterns() {
        assertTrue(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("Bankgiro 1234-5678").matches());
        assertTrue(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("Bankgiro 123-4567").matches());
        assertTrue(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("Postgiro 123456-7").matches());
        assertTrue(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("Postgiro 123-7").matches());
        assertTrue(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("Postgiro 4-2").matches());
    }

    @Test
    public void testOtherPatterns() {
        assertTrue(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("E-giro (PG) 123456-7").matches());
        assertTrue(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("E-giro 123-4567").matches());
        assertTrue(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("E-giro 1234-5678").matches());
    }

    @Test
    public void testDoesntMatchEachOther() {
        assertFalse(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("Bankgiro 1234-567").matches());
        assertFalse(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("Bankgiro 1234-5678").matches());
        assertFalse(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("Postgiro 123456-7").matches());
    }

    @Test
    public void testMatchOnlyDigits() {
        assertTrue(RecipientAccountEntity.PATTERN_PG_RECIPIENT.matcher("123456-7").matches());
        assertTrue(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("123-4567").matches());
        assertTrue(RecipientAccountEntity.PATTERN_BG_RECIPIENT.matcher("1234-5678").matches());
    }

}