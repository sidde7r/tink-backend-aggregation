package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SdcAmountTest {

    private static final long VALUE = 234L;
    private static final int SCALE = 3;
    private static final String CURRENCY = "DKK";

    private static final String SDC_AMOUNT =
            "{\n"
                    + "    \"value\": "
                    + VALUE
                    + ","
                    + "    \"scale\": "
                    + SCALE
                    + ","
                    + "    \"currency\": \""
                    + CURRENCY
                    + "\""
                    + "}";

    private static final String SDC_AMOUNT_WO_CURRENCY =
            "{\n" + "    \"value\": " + VALUE + "," + "    \"scale\": " + SCALE + "}";

    @Test
    public void toExactCurrencyAmount() {
        // given
        SdcAmount sdcAmount = SerializationUtils.deserializeFromString(SDC_AMOUNT, SdcAmount.class);

        // when
        ExactCurrencyAmount result = sdcAmount.toExactCurrencyAmount();

        // then
        assertThat(result.getCurrencyCode()).isEqualTo(CURRENCY);
        assertThat(result.getExactValue()).isEqualTo(BigDecimal.valueOf(VALUE, SCALE));
    }

    @Test
    public void toExactCurrencyAmountWithDefaultValuePassedThatShouldNotBeUnused() {
        // given
        SdcAmount sdcAmount = SerializationUtils.deserializeFromString(SDC_AMOUNT, SdcAmount.class);
        // and
        String defaultCurrency = "OTHER CURRENCY";

        // when
        ExactCurrencyAmount result = sdcAmount.toExactCurrencyAmount(defaultCurrency);

        // then
        assertThat(result.getCurrencyCode()).isEqualTo(CURRENCY);
        assertThat(result.getExactValue()).isEqualTo(BigDecimal.valueOf(VALUE, SCALE));
    }

    @Test
    public void toExactCurrencyAmountWithDefaultValuePassedThatShouldBeUnused() {
        // given
        SdcAmount sdcAmount =
                SerializationUtils.deserializeFromString(SDC_AMOUNT_WO_CURRENCY, SdcAmount.class);
        // and
        String defaultCurrency = "OTHER CURRENCY";

        // when
        ExactCurrencyAmount result = sdcAmount.toExactCurrencyAmount(defaultCurrency);

        // then
        assertThat(result.getCurrencyCode()).isEqualTo(defaultCurrency);
        assertThat(result.getExactValue()).isEqualTo(BigDecimal.valueOf(VALUE, SCALE));
    }
}
