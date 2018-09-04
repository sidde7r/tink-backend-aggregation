package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v45;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;

public class AmericanExpressSEConstants extends AmericanExpressConstants {

    public static class HeaderValues {
        public static final String APP_ID = "se.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20SE/4.4.1 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "sv_SE";
    }

    public static class BodyValue {
        public static final String LOCALE = "sv_SE";
        public static final String CLIENT_VERSION = "4.4.1";
    }
}
