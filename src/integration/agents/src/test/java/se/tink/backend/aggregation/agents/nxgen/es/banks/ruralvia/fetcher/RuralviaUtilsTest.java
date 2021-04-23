package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;
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
        assertTrue(result.getCurrencyCode().equals(currency));
        assertTrue(result.getStringValue(locale).equals("100,00"));

        assertTrue(result2.getStringValue(locale).equals("1.234.567.890.000,12"));
    }

    @Test
    public void parseAmountInEurosShouldParse() {
        // given
        Locale locale = new Locale("es", "ES");
        String amountToClean = "\n100 , 00 \t";

        // when
        ExactCurrencyAmount result = RuralviaUtils.parseAmountInEuros(amountToClean);

        // then
        assertTrue(result.getCurrencyCode().equals("EUR"));
        assertTrue(result.getStringValue(locale).equals("100,00"));
    }

    @Test
    public void getURLEncodedUTF8StringShouldEncodeWhenReceiveString(String toencode) {
        fail();
    }
}
