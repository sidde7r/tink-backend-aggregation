package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;

public class AmericanExpressV62ATConstants extends AmericanExpressConstants {

    public static class HeaderValues {
        public static final String APP_ID = "at.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20AT/15 CFNetwork/811.4.18 Darwin/16.5.0";
        public static final String FACE = "de_AT";
    }

    public static class BodyValue {
        public static final String LOCALE = "de_AT";
        public static final String CLIENT_VERSION = "4.4.1";
    }
}
