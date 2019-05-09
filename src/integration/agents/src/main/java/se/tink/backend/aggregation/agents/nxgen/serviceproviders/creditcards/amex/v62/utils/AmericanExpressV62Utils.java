package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.DASH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.NOT_APPLICABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.NUMBER_REGEX;

import se.tink.libraries.strings.StringUtils;

public class AmericanExpressV62Utils {
    public static boolean isValidAmount(final String value) {
        return !DASH.equalsIgnoreCase(value) && !NOT_APPLICABLE.equalsIgnoreCase(value);
    }

    public static double parseAmount(final String value) {
        return StringUtils.parseAmount(value.replaceAll(NUMBER_REGEX, ""));
    }
}
