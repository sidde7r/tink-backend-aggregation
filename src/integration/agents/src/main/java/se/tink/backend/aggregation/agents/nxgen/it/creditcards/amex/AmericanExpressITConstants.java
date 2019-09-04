package se.tink.backend.aggregation.agents.nxgen.it.creditcards.amex;

public final class AmericanExpressITConstants {

    private AmericanExpressITConstants() {
        throw new AssertionError();
    }

    public static final String MARKET = "IT";
    public static final String PROVIDER_NAME = "it-americanexpress-password";

    public static class HeaderValues {
        public static final String APP_ID = "it.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20IT/6.10.0 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "it_IT";
    }

    public static class BodyValue {
        public static final String LOCALE = "it_IT";
        public static final String CLIENT_VERSION = "6.10.0";
    }
}
