package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.libraries.amount.Amount;

public class EuroInformationUtilsTest {

    public static final String CURRENCY_INPUT = "EUR";

    @Test
    public void parseAmount_whenCurrencySpecified() {
        Amount amount = EuroInformationUtils.parseAmount("+23.30SEK", CURRENCY_INPUT);
        assertEquals(Amount.inEUR(23.30d), amount);
    }

    @Test
    public void parseAmount_whenCurrencyNotSpecified() {
        Amount amount = EuroInformationUtils.parseAmount("+23.30SEK");
        assertEquals(Amount.inSEK(23.30d), amount);
    }

    @Test
    public void parseAmount_withNegativeValue() {
        Amount amount = EuroInformationUtils.parseAmount("-23.30SEK", CURRENCY_INPUT);
        assertEquals(Amount.inEUR(-23.30d), amount);
    }

    @Test
    public void parseAmount_zeroWithSign() {
        Amount amount = EuroInformationUtils.parseAmount("+0.00SEK", CURRENCY_INPUT);
        assertEquals(Amount.inEUR(0.00d), amount);
    }

    @Test
    public void parseAmount_zeroWithoutSign() {
        Amount amount = EuroInformationUtils.parseAmount("0.00SEK", CURRENCY_INPUT);
        assertEquals(Amount.inEUR(0.00d), amount);
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
