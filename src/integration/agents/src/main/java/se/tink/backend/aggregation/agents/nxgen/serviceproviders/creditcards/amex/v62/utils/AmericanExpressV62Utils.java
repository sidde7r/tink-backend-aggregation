package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils;

import java.math.BigDecimal;
import se.tink.libraries.strings.StringUtils;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.DASH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.NOT_APPLICABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.NUMBER_REGEX;

public class AmericanExpressV62Utils {
    public static boolean isValidAmount(final String value) {
        return !DASH.equalsIgnoreCase(value) && !NOT_APPLICABLE.equalsIgnoreCase(value);
    }

    public static double parseAmount(final String value) {
        return StringUtils.parseAmount(value.replaceAll(NUMBER_REGEX, ""));
    }

    public static BigDecimal parseAmountToBigDecimal(final String value) {
        return BigDecimal.valueOf(StringUtils.parseAmount(value.replaceAll(NUMBER_REGEX, "")));
    }
}
