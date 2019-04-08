package se.tink.backend.aggregation.agents.nxgen.be.creditcards.amex;

public class AmericanExpressBEConstants {

    public static final String MARKET = "BE";
    public static final String PROVIDER_NAME = "be-americanexpress-password";

    public static class HeaderValues {
        public static final String APP_ID = "be.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20BE/4.4.1 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "fr_BE";
    }

    public static class BodyValue {
        public static final String LOCALE = "en_BE";
        public static final String CLIENT_VERSION = "4.5.0";
    }
}
