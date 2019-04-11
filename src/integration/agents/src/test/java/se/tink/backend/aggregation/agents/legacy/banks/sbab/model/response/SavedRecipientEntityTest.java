package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class SavedRecipientEntityTest {

    @Test
    public void testCorrectHandlingOfRecipientLabels() {

        // BI = internal transfer.
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString("12345|BI|TestName");
        Assert.assertEquals(savedRecipientEntity.get().getLabel(), "BI");

        // BE = internal transfer.
        savedRecipientEntity = SavedRecipientEntity.createFromString("12345|BE|TestName");
        Assert.assertEquals(savedRecipientEntity.get().getLabel(), "BE");
    }

    @Test
    public void testRecipientNamesWithSpaces() {
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString("12345|BI|Test name with spaces");
        Assert.assertTrue(savedRecipientEntity.isPresent());
        Assert.assertEquals(savedRecipientEntity.get().getAccountNumber(), "12345");
        Assert.assertEquals(savedRecipientEntity.get().getLabel(), "BI");
        Assert.assertEquals(savedRecipientEntity.get().getName(), "Test name with spaces");
    }

    @Test
    public void testRecipientNamesWithStrangeCharacters() {
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString(
                        "12345|BI|Test (name with str4nge characters %€#)");
        Assert.assertTrue(savedRecipientEntity.isPresent());
        Assert.assertEquals(savedRecipientEntity.get().getAccountNumber(), "12345");
        Assert.assertEquals(savedRecipientEntity.get().getLabel(), "BI");
        Assert.assertEquals(
                savedRecipientEntity.get().getName(), "Test (name with str4nge characters %€#)");
    }

    @Test
    public void testRecipientNamesWithSeparatorAsCharacter() {
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString(
                        "12345|BI|Test name with separator | which is not allowed");
        Assert.assertFalse(savedRecipientEntity.isPresent());
    }

    @Test
    /** Expected: error log. */
    public void testWrongSeparatorInRecipientString() {
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString("12345 BI TestName");
        Assert.assertFalse(savedRecipientEntity.isPresent());
    }

    @Test
    /** Expected: error log. */
    public void testNonNumericAccountNumberInRecipientString() {
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString("SE12345|BI|TestName");
        Assert.assertFalse(savedRecipientEntity.isPresent());
    }

    @Test
    /** Expected: error log. */
    public void testAccountNumberWithDashInRecipientString() {
        Optional<SavedRecipientEntity> savedRecipientEntity =
                SavedRecipientEntity.createFromString("123-45|BI|TestName");
        Assert.assertFalse(savedRecipientEntity.isPresent());
    }
}
