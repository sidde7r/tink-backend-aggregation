package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;

public class AmericanExpressDEConstants extends AmericanExpressConstants {

    public static final String MARKET = "DE";
    public static final String PROVIDER_NAME = "de-americanexpress-password";

    public static class HeaderValues {
        public static final String APP_ID = "de.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20DE/4.4.1 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "de_DE";
    }

    public static class BodyValue {
        public static final String LOCALE = "de_DE";
        public static final String CLIENT_VERSION = "4.4.1";
    }
}
