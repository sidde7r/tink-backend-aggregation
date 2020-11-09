package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class EuroInformationUtilsTest {

    public static final String CURRENCY_INPUT = "EUR";

    @Test
    public void parseAmount_whenCurrencySpecified() {
        ExactCurrencyAmount amount = EuroInformationUtils.parseAmount("+23.30SEK", CURRENCY_INPUT);
        assertEquals(ExactCurrencyAmount.inEUR(23.30), amount);
    }

    @Test
    public void parseAmount_whenCurrencyNotSpecified() {
        ExactCurrencyAmount amount = EuroInformationUtils.parseAmount("+23.30SEK");
        assertEquals(ExactCurrencyAmount.inSEK(23.30), amount);
    }

    @Test
    public void parseAmount_withNegativeValue() {
        ExactCurrencyAmount amount = EuroInformationUtils.parseAmount("-23.30SEK", CURRENCY_INPUT);
        assertEquals(ExactCurrencyAmount.inEUR(-23.30), amount);
    }

    @Test
    public void parseAmount_zeroWithSign() {
        ExactCurrencyAmount amount = EuroInformationUtils.parseAmount("+0.00SEK", CURRENCY_INPUT);
        assertEquals(ExactCurrencyAmount.inEUR(0.00), amount);
    }

    @Test
    public void parseAmount_zeroWithoutSign() {
        ExactCurrencyAmount amount = EuroInformationUtils.parseAmount("0.00SEK", CURRENCY_INPUT);
        assertEquals(ExactCurrencyAmount.inEUR(0.00), amount);
    }

    @Test
    public void isSuccess_whenLoginErrorCode() {
        assertFalse(
                EuroInformationUtils.isSuccess(
                        EuroInformationErrorCodes.LOGIN_ERROR.getCodeNumber()));
    }

    @Test
    public void isSuccess_whenSuccessCode() {
        assertTrue(
                EuroInformationUtils.isSuccess(EuroInformationErrorCodes.SUCCESS.getCodeNumber()));
    }

    @Test
    public void isSuccess_whenUnknownErrorCode() {
        assertFalse(EuroInformationUtils.isSuccess("555555"));
    }
}
