package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher;

import static org.junit.Assert.assertEquals;

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
        assertEquals(result.getCurrencyCode(), currency);
        assertEquals(result.getStringValue(locale), "100,00");

        assertEquals(result2.getStringValue(locale), "1.234.567.890.000,12");
    }

    @Test
    public void parseAmountInEurosShouldParse() {
        // given
        Locale locale = new Locale("es", "ES");
        String amountToClean = "\n100 , 00 \t";

        // when
        ExactCurrencyAmount result = RuralviaUtils.parseAmountInEuros(amountToClean);

        // then
        assertEquals(result.getCurrencyCode(), "EUR");
        assertEquals(result.getStringValue(locale), "100,00");
    }

    @Test
    public void getURLEncodedUTF8StringShouldEncodeWhenReceiveString() {
        // given
        String urlToEncode = "url to?encode& ";
        String urlToEncode2 = "κόσμε";

        // when
        String result = RuralviaUtils.getURLEncodedUTF8String(urlToEncode);
        String result2 = RuralviaUtils.getURLEncodedUTF8String(urlToEncode2);

        // then
        assertEquals(result, "url+to%3Fencode%26+");
        assertEquals(result2, "%CE%BA%E1%BD%B9%CF%83%CE%BC%CE%B5");
    }
}
