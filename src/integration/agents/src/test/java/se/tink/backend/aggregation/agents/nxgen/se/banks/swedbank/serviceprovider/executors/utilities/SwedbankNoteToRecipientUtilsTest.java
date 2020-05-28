package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SwedbankNoteToRecipientUtilsTest {

    @Test
    public void testSpaceAllowed() {
        assertTrue(SwedbankNoteToRecipientUtils.isValidSwedbankNoteToRecipient("Test_ AB-"));
    }

    @Test
    public void testValidCase() {
        assertTrue(SwedbankNoteToRecipientUtils.isValidSwedbankNoteToRecipient("IOPåäölmn"));
    }

    @Test
    public void testSymbolNotAllowed() {
        assertFalse(SwedbankNoteToRecipientUtils.isValidSwedbankNoteToRecipient("LÅN April&Maj"));
    }

    @Test
    public void testSymbolsNotAllowed() {
        assertFalse(SwedbankNoteToRecipientUtils.isValidSwedbankNoteToRecipient("<>%"));
    }

    @Test
    public void testAllStrangeCharsAllowed() {
        assertTrue(
                SwedbankNoteToRecipientUtils.isValidSwedbankNoteToRecipient("!@#$*()_-=,_[]\\/"));
    }
}
