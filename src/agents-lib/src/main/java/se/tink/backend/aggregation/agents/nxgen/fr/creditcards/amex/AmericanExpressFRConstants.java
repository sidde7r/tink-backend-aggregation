package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;

public class AmericanExpressFRConstants extends AmericanExpressConstants {

    public static class HeaderValues {
        public static final String APP_ID = "fr.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FR/4.5.0 CFNetwork/808.4.18 Darwin/16.5.0";
        public static final String FACE = "fr_FR";
    }

    public static class BodyValues {
        public static final String LOCALE = "fr_FR";
        public static final String CLIENT_VERSION = "4.5.0";
    }
}
