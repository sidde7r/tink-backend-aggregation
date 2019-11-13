package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.DASH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.NOT_APPLICABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.NUMBER_REGEX;

import com.google.gson.Gson;
import java.math.BigDecimal;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressV62Utils {

    private static final Gson GSON = new Gson();

    public static boolean isValidAmount(final String value) {
        return !DASH.equalsIgnoreCase(value) && !NOT_APPLICABLE.equalsIgnoreCase(value);
    }

    public static double parseAmount(final String value) {
        return StringUtils.parseAmount(value.replaceAll(NUMBER_REGEX, ""));
    }

    public static BigDecimal parseAmountToBigDecimal(final String value) {
        return BigDecimal.valueOf(StringUtils.parseAmount(value.replaceAll(NUMBER_REGEX, "")));
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
