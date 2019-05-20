package se.tink.libraries.account.identifiers;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.libraries.account.AccountIdentifier;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaymPhoneNumberIdentifierTest {

    private final PaymPhoneNumberIdentifier VALID_IDENTIFIER =
            new PaymPhoneNumberIdentifier("01222555555");
    private final PaymPhoneNumberIdentifier INVALID_IDENTIFIER =
            new PaymPhoneNumberIdentifier("111111111");

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void isValid_returnsTrue_ifValidIdentifier() {
        assertTrue(VALID_IDENTIFIER.isValid());
    }

    @Test
    public void isValid_returnsFalse_ifNotValidIdentifier() {
        assertFalse(INVALID_IDENTIFIER.isValid());
    }

    @Test
    public void getType_returnsCorrectType_ifValidSortCodeIdentifier() {
        Assert.assertEquals(VALID_IDENTIFIER.getType(), AccountIdentifier.Type.PAYM_PHONE_NUMBER);
    }

    @Test
    public void constructor_throwsException_ifNullIdentifierProvided() {
        expectedException.expect(IllegalArgumentException.class);
        new SortCodeIdentifier(null);
    }
}
