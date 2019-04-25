package se.tink.libraries.account.identifiers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.libraries.account.AccountIdentifier;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
        assertThat(VALID_IDENTIFIER.getType(), is(AccountIdentifier.Type.PAYMENT_CARD_NUMBER));
    }

    @Test
    public void constructor_throwsException_ifNullIdentifierProvided() {
        expectedException.expect(IllegalArgumentException.class);
        new SortCodeIdentifier(null);
    }
}
