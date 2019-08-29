package se.tink.libraries.amount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;

public class ExactCurrencyAmountTest {
    @Test
    public void createFromBigDecimal() {
        ExactCurrencyAmount amount = new ExactCurrencyAmount(BigDecimal.valueOf(12.3d), "SEK");

        assertEquals(123, amount.getUnscaledValue());
        assertEquals(1, amount.getScale());
        assertEquals("SEK", amount.getCurrencyCode());
    }

    @Test
    public void reflexiveEquality() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");

        assertTrue(a.equals(a));
    }

    @Test
    public void symmetricEquality() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    public void transitiveEquality() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(3.0), "SEK");
        ExactCurrencyAmount c = ExactCurrencyAmount.of(BigDecimal.valueOf(3.00), "SEK");

        // a == b & b == c => a = c
        assertEquals(a, b);
        assertEquals(b, c);
    }

    @Test
    public void consistentEquality() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");

        assertTrue(a.equals(b));
        assertTrue(a.equals(b));
    }

    @Test
    public void equalsReturnTrueForEqualAmountsWithBothCurrencyCodeNull() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), null);
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(3), null);
        assertTrue(a.equals(b));
    }

    @Test
    public void equalsReturnFalseComparing3SekWith3Null() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(3), null);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void nullEquality() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");

        assertFalse(a.equals(null));
    }

    @Test
    public void differentCurrency() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "NOK");

        assertFalse(a.equals(b));
    }

    @Test
    public void differentAmount() {
        ExactCurrencyAmount a = ExactCurrencyAmount.of(BigDecimal.valueOf(3), "SEK");
        ExactCurrencyAmount b = ExactCurrencyAmount.of(BigDecimal.valueOf(45), "SEK");

        assertFalse(a.equals(b));
    }
}
