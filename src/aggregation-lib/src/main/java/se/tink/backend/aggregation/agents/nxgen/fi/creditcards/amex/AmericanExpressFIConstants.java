package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.AmericanExpressConstants;

public class AmericanExpressFIConstants extends AmericanExpressConstants {

    public static class HeaderValues {
        public static final String APP_ID = "fi.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FI/4.5.0 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "fi_FI";
    }

    public static class BodyValues {
        public static final String LOCALE = "fi_FI";
        public static final String CLIENT_VERSION = "4.5.0";
    }
}
