package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils;

import com.google.gson.Gson;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.libraries.strings.StringUtils;

public class AmericanExpressV62Utils {

    private static final Gson GSON = new Gson();

    public static boolean isValidAmount(final String value) {
        return !AmericanExpressV62Constants.DASH.equalsIgnoreCase(value)
                && !AmericanExpressV62Constants.NOT_APPLICABLE.equalsIgnoreCase(value);
    }

    public static BigDecimal parseAmountToBigDecimal(final String value) {
        return BigDecimal.valueOf(
                StringUtils.parseAmount(
                        value.replaceAll(AmericanExpressV62Constants.NUMBER_REGEX, "")));
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
