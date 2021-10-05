package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SwedbankSESerializationUtilsTest {

    @Test
    public void shouldThrowIllegalArgumentIfAmountIsNotValidAmount() {
        Throwable t =
                catchThrowable(
                        () ->
                                SwedbankSeSerializationUtils.parseAmountForInput(
                                        "random String", "SEK"));

        assertThat(t).isExactlyInstanceOf(IllegalArgumentException.class);
        assertEquals("Cannot parse amount: random String", t.getMessage());
    }

    @Test
    public void shouldGetExactAmountFromNegativeAmount() {
        ExactCurrencyAmount result =
                SwedbankSeSerializationUtils.parseAmountForInput("-200.00", "SEK");

        assertEquals(result, ExactCurrencyAmount.of(-200, "SEK"));
    }

    @Test
    public void shouldGetExactAmountInUSD() {
        ExactCurrencyAmount result =
                SwedbankSeSerializationUtils.parseAmountForInput("200.00", "USD");

        assertEquals(result, ExactCurrencyAmount.of(200, "USD"));
    }

    @Test
    public void shouldGetExactAmountInSEKifCurrencyIsUnspecified() {
        ExactCurrencyAmount result =
                SwedbankSeSerializationUtils.parseAmountForInput("2000000.00", null);

        assertEquals(result, ExactCurrencyAmount.of(2000000, "SEK"));
    }

    @Test
    public void shouldThrowIllegalArgumentIfInterestRateIsNotValid() {
        Throwable t =
                catchThrowable(
                        () -> SwedbankSeSerializationUtils.parseInterestRate("random String"));

        assertThat(t).isExactlyInstanceOf(IllegalArgumentException.class);
        assertEquals("Cannot parse interest rate: random String", t.getMessage());
    }

    @Test
    public void shouldGetInterestRateIfInputIsValidRate() {
        Double result = SwedbankSeSerializationUtils.parseInterestRate("3.0%");

        assertEquals(result, Double.valueOf("0.03"));
    }

    @Test
    public void shouldThrowIllegalArgumentIfInterestRateMissingPercentage() {
        Throwable t = catchThrowable(() -> SwedbankSeSerializationUtils.parseInterestRate("3.0"));

        assertThat(t).isExactlyInstanceOf(IllegalArgumentException.class);
        assertEquals("Cannot parse interest rate: 3.0", t.getMessage());
    }

    @Test
    public void shouldReturnMonthsForYears() {
        int result = SwedbankSeSerializationUtils.parseNumMonthsBound("3 år");

        assertEquals(36, result);
    }

    @Test
    public void shouldReturnMonthsForMonths() {
        int result = SwedbankSeSerializationUtils.parseNumMonthsBound("3 mån");

        assertEquals(3, result);
    }

    @Test
    public void shouldReturnZeroIfRandomString() {
        int result = SwedbankSeSerializationUtils.parseNumMonthsBound("random String");

        assertEquals(0, result);
    }
}
