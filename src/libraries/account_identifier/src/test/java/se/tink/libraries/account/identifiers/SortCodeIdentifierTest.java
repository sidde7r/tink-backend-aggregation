package se.tink.libraries.account.identifiers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.libraries.account.AccountIdentifier;

public class SortCodeIdentifierTest {

    private static final SortCodeIdentifier VALID_IDENTIFIER_7_DIGIT_ACCOUNT_NUMBER =
            new SortCodeIdentifier("12-34-56 1234567");
    private static final SortCodeIdentifier VALID_IDENTIFIER_8_DIGIT_ACCOUNT_NUMBER =
            new SortCodeIdentifier("12-3456    12345678");

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void
            getIdentifier_shouldReturnIdentifier_ifValidIdentifierWithSevenDigitAccountNumber() {
        assertThat(VALID_IDENTIFIER_7_DIGIT_ACCOUNT_NUMBER.getIdentifier(), is("12345601234567"));
    }

    @Test
    public void getIdentifier_returnsIdentifier_ifValidIdentifierWithEightDigitAccountNumber() {
        assertThat(VALID_IDENTIFIER_8_DIGIT_ACCOUNT_NUMBER.getIdentifier(), is("12345612345678"));
    }

    @Test
    public void isValid_returnsTrue_ifValidIdentifier() {
        assertTrue(VALID_IDENTIFIER_7_DIGIT_ACCOUNT_NUMBER.isValid());
    }

    @Test
    public void getType_returnsCorrectType_ifValidSortCodeIdentifier() {
        assertThat(
                VALID_IDENTIFIER_7_DIGIT_ACCOUNT_NUMBER.getType(),
                is(AccountIdentifier.Type.SORT_CODE));
    }

    @Test
    public void getAccountNumber_returnsAccountNumber_ifValidIdentifier() {
        assertThat(VALID_IDENTIFIER_7_DIGIT_ACCOUNT_NUMBER.getAccountNumber(), is("1234567"));
    }

    @Test
    public void getSortCode_returnsSortCode_ifValidIdentifier() {
        assertThat(VALID_IDENTIFIER_7_DIGIT_ACCOUNT_NUMBER.getSortCode(), is("123456"));
    }

    @Test
    public void constructor_throwsException_ifNullIdentifierProvided() {
        expectedException.expect(IllegalArgumentException.class);
        new SortCodeIdentifier(null);
    }

    @Test
    public void constructor_throwsException_ifInvalidIdentifierProvided() {
        expectedException.expect(IllegalArgumentException.class);
        new SortCodeIdentifier("1234");
    }
}
