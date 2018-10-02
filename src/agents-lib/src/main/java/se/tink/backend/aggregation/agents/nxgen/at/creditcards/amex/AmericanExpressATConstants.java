package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;

public class AmericanExpressATConstants extends AmericanExpressConstants {

    public static final String MARKET = "AT";
    public static final String PROVIDER_NAME = "at-americanexpress-password";

    public static class HeaderValues {
        public static final String APP_ID = "at.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20AT/4.4.1 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "de_AT";
    }

    public static class BodyValue {
        public static final String LOCALE = "de_AT";
        public static final String CLIENT_VERSION = "4.4.1";
    }

}
