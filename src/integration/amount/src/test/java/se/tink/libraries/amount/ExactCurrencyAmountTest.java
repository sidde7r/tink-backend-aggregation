package se.tink.libraries.amount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;
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

    @Test
    public void testStringValueHasProperFormat() {
        ExactCurrencyAmount value1 = ExactCurrencyAmount.of(new BigDecimal("1"), "GBP");
        ExactCurrencyAmount value2 = ExactCurrencyAmount.of(new BigDecimal("1.00"), "GBP");
        ExactCurrencyAmount value3 = ExactCurrencyAmount.of(new BigDecimal("1.1"), "GBP");
        ExactCurrencyAmount value4 = ExactCurrencyAmount.of(new BigDecimal("100000"), "GBP");
        ExactCurrencyAmount value5 = ExactCurrencyAmount.of(new BigDecimal("1.0123"), "GBP");

        assertThat(value1.getStringValue(Locale.UK)).isEqualTo("1.00");
        assertThat(value2.getStringValue(Locale.UK)).isEqualTo("1.00");
        assertThat(value3.getStringValue(Locale.UK)).isEqualTo("1.10");
        assertThat(value4.getStringValue(Locale.UK)).isEqualTo("100,000.00");
        assertThat(value5.getStringValue(Locale.UK)).isEqualTo("1.01");
    }

    @Test
    public void compareTo() {
        // given
        ExactCurrencyAmount value1 = ExactCurrencyAmount.of(new BigDecimal("1"), "GBP");
        ExactCurrencyAmount value2 = ExactCurrencyAmount.of(new BigDecimal("1"), "GBP");
        ExactCurrencyAmount value3 = ExactCurrencyAmount.of(new BigDecimal("10"), "GBP");

        // when
        int result1 = value1.compareTo(value2);
        int result2 = value1.compareTo(value3);
        int result3 = value3.compareTo(value1);

        // then
        assertThat(result1).isZero();
        assertThat(result2).isNegative();
        assertThat(result3).isPositive();
    }

    @Test
    public void zeroStaticMethodReturnsZeroWithExpectedCurrency() {
        // given
        String currencyEur = "EUR";
        String currencyPln = "PLN";

        // when
        ExactCurrencyAmount zeroEur = ExactCurrencyAmount.zero(currencyEur);
        ExactCurrencyAmount zeroPln = ExactCurrencyAmount.zero(currencyPln);

        // then
        assertThat(zeroEur.getExactValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(zeroEur.getCurrencyCode()).isEqualTo(currencyEur);

        assertThat(zeroPln.getExactValue()).isEqualTo(BigDecimal.ZERO);
        assertThat(zeroPln.getCurrencyCode()).isEqualTo(currencyPln);
    }
}
