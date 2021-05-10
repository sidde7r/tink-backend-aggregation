package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public final class RuralviaUtils {

    private RuralviaUtils() {}

    public static ExactCurrencyAmount parseAmount(String amountString, String currency) {
        final String amountWithoutSpaces = amountString.replaceAll("[\\s\\u00a0]+", "");

        try {
            NumberFormat nf = NumberFormat.getInstance(new Locale("es", "ES"));
            return ExactCurrencyAmount.of(
                    new BigDecimal(nf.parse(amountWithoutSpaces).toString()), currency);
        } catch (ParseException e) {
            log.warn("WARN: parsing the value was not possible", e);
        }
        return ExactCurrencyAmount.inEUR(0);
    }

    public static ExactCurrencyAmount parseAmountInEuros(String amountString) {
        return parseAmount(amountString, "EUR");
    }

    public static String extractToken(
            String html, String startToSearchAt, String searchFor, String searchStop) {
        int beginIndex = html.indexOf(startToSearchAt);
        beginIndex = html.indexOf(searchFor, beginIndex) + searchFor.length();
        int endIndex = html.indexOf(searchStop, beginIndex);
        return html.substring(beginIndex, endIndex);
    }
}
