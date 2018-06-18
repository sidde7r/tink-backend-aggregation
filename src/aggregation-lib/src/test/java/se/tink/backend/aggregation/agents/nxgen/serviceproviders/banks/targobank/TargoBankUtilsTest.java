package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.core.Amount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargoBankUtilsTest {

    public static final String CURRENCY_INPUT = "EUR";

    @Test
    public void parseAmount_whenCurrencySpecified() {
        Amount amount = TargoBankUtils.parseAmount("+23.30SEK", CURRENCY_INPUT);
        assertEquals(Amount.inEUR(23.30d), amount);
    }

    @Test
    public void parseAmount_whenCurrencyNotSpecified() {
        Amount amount = TargoBankUtils.parseAmount("+23.30SEK");
        assertEquals(Amount.inSEK(23.30d), amount);
    }

    @Test
    public void parseAmount_withNegativeValue() {
        Amount amount = TargoBankUtils.parseAmount("-23.30SEK", CURRENCY_INPUT);
        assertEquals(Amount.inEUR(-23.30d), amount);
    }

    @Test
    public void isSuccess_whenLoginErrorCode() {
        assertFalse(TargoBankUtils.isSuccess(TargoBankErrorCodes.LOGIN_ERROR.getCodeNumber()));
    }

    @Test
    public void isSuccess_whenSuccessCode() {
        assertTrue(TargoBankUtils.isSuccess(TargoBankErrorCodes.SUCCESS.getCodeNumber()));
    }

    @Test
    public void isSuccess_whenUnknownErrorCode() {
        assertFalse(TargoBankUtils.isSuccess("555555"));
    }
}
