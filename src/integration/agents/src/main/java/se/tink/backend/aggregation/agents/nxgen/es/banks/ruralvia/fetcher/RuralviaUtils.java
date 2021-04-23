package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher;

import com.google.common.annotations.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@UtilityClass
public class RuralviaUtils {

    @VisibleForTesting
    public static ExactCurrencyAmount parseAmount(String amountString, String currency) {
        final String amountWithoutSpaces = amountString.replaceAll("[\\s\\u00a0]+", "");

        try {
            NumberFormat nf = NumberFormat.getInstance(Locale.ITALY);
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

    public static String getURLEncodedUTF8String(String toencode) {
        try {
            return URLEncoder.encode(toencode, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.warn("WARN: url encoding failed for params", e);
            return "";
        }
    }
}
