package se.tink.libraries.account.identifiers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.libraries.account.AccountIdentifier;

public class PaymentCardNumberIdentiferTest {

    private final PaymentCardNumberIdentifier VALID_IDENTIFIER =
            new PaymentCardNumberIdentifier("79927398713");

    private final PaymentCardNumberIdentifier INVALID_IDENTIFIER =
            new PaymentCardNumberIdentifier("7992739820");

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
        Assert.assertEquals(VALID_IDENTIFIER.getType(), AccountIdentifier.Type.PAYMENT_CARD_NUMBER);
    }

    @Test
    public void constructor_throwsException_ifNullIdentifierProvided() {
        expectedException.expect(IllegalArgumentException.class);
        new SortCodeIdentifier(null);
    }
}
