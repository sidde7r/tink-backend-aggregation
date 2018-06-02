package se.tink.backend.core;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AmountTest {

    @Test
    public void testNullAmountEquality() {
        assertTrue(Amount.inSEK(null).equals(Amount.inSEK(null)));
        assertTrue(Amount.inEUR(null).equals(Amount.inEUR(null)));
        assertFalse(Amount.inEUR(null).equals(Amount.inSEK(null)));
        assertFalse(Amount.inSEK(null).equals(Amount.inEUR(null)));
    }

    @Test
    public void testNullCurrencyEquality() {
        assertTrue(new Amount(null, 0d).equals(new Amount(null, 0d)));
        assertTrue(new Amount(null, 1.05).equals(new Amount(null, 1.05)));
        assertTrue(new Amount(null, null).equals(new Amount(null, null)));
        assertTrue(new Amount(null, -0.4).equals(new Amount(null, -0.4)));
        assertFalse(new Amount(null, -0.4).equals(new Amount(null, -0.45)));
        assertFalse(new Amount(null, 0.d).equals(new Amount(null, 12312.2)));
        assertFalse(new Amount(null, null).equals(new Amount(null, 12312.2)));
    }

    @Test
    public void testEquality() {
        assertTrue(Amount.inSEK(0.5).equals(Amount.inSEK(0.5)));
        assertTrue(Amount.inEUR(1.0).equals(Amount.inEUR(1.0)));

    }

    @Test
    public void testInequality() {
        assertFalse(Amount.inSEK(0.5).equals(Amount.inSEK(0.51)));
        assertFalse(Amount.inEUR(1.0).equals(Amount.inEUR(1.000001)));
        assertFalse(Amount.inEUR(5.0).equals(Amount.inSEK(5.0)));
        assertFalse(Amount.inSEK(5.0).equals(Amount.inEUR(5.0)));
    }

    @Test
    public void testEmptyIfAmountOrAnyFieldIsNull() {
        assertNull(Amount.createFromAmount(null).orElse(null));
        assertNull(Amount.createFromAmount(new Amount(null, null)).orElse(null));
        assertNull(Amount.createFromAmount(new Amount("SEK", null)).orElse(null));
        Amount newAmount = new Amount("SEK", 1d);
        assertNotNull(Amount.createFromAmount(newAmount).orElse(null));
        // Assert that it is not the same instance
        assertFalse(Amount.createFromAmount(newAmount).orElse(null) == newAmount);
    }

    @Test
    public void testIntGreaterThan() {
        Amount amount = new Amount("SEK", 3d);
        assertTrue(amount.isGreaterThan(1));
    }

    @Test
    public void testIntLessThan() {
        Amount amount = new Amount("SEK", 1d);
        assertTrue(amount.isLessThan(3));
    }

    @Test
    public void testDoubleGreaterThan() {
        Amount amount = new Amount("SEK", 3d);
        assertTrue(amount.isGreaterThan(1d));
    }

    @Test
    public void testDoubleLessThan() {
        Amount amount = new Amount("SEK", 1d);
        assertTrue(amount.isLessThan(3d));
    }

    @Test
    public void testAddition() {
        Amount amount = Amount.inSEK(5d);
        assertTrue(amount.add(4d).isGreaterThan(8d));
        assertTrue(amount.add(Amount.inSEK(4d)).isGreaterThan(8d));
    }

    @Test
    public void testSubtraction() {
        Amount amount = Amount.inSEK(5d);
        assertTrue(amount.subtract(4d).isLessThan(2d));
        assertTrue(amount.subtract(Amount.inSEK(4d)).isLessThan(2d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIllegalArgument_whenAdditionWithDifferentCurrency() {
        Amount amount = Amount.inSEK(5d);
        amount.add(Amount.inDKK(1d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIllegalArgument_whenSubtractionWithDifferentCurrency() {
        Amount amount = Amount.inSEK(5d);
        amount.subtract(Amount.inDKK(1d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIllegalArgument_whenAdditionWithNegativeValue() {
        Amount amount = Amount.inSEK(5d);
        amount.add(Amount.inSEK(-1d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureIllegalArgument_whenSubtractionWithNegativeValue() {
        Amount amount = Amount.inSEK(5d);
        amount.subtract(Amount.inSEK(-1d));
    }
}
