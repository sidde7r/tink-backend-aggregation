package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class RuralviaUtilsTest {

    @Test
    public void parseAmountShouldParse() {
        // given
        Locale locale = new Locale("es", "ES");
        String amountToClean = "\t\t 100, 00 \n";
        String currency = "EUR";

        String amount2 = "1234567890000,1234567";

        // when
        ExactCurrencyAmount result = RuralviaUtils.parseAmount(amountToClean, currency);
        ExactCurrencyAmount result2 = RuralviaUtils.parseAmount(amount2, currency);

        // then
        assertEquals(currency, result.getCurrencyCode());
        assertEquals("100,00", result.getStringValue(locale));

        assertEquals("1.234.567.890.000,12", result2.getStringValue(locale));
    }

    @Test
    public void parseAmountInEurosShouldParse() {
        // given
        Locale locale = new Locale("es", "ES");
        String amountToClean = "\n100 , 00 \t";

        // when
        ExactCurrencyAmount result = RuralviaUtils.parseAmountInEuros(amountToClean);

        // then
        assertEquals("EUR", result.getCurrencyCode());
        assertEquals("100,00", result.getStringValue(locale));
    }

    @Test
    public void extractTokenShouldReturnToken() {
        // given
        String text =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";

        // when
        String result1 = RuralviaUtils.extractToken(text, ". Ut", "nostrud ", " ullamco");
        String result2 = RuralviaUtils.extractToken(text, "Lorem", " ", " d");

        // then
        Assert.assertEquals("exercitation", result1);
        Assert.assertEquals("ipsum", result2);
    }
}
