package se.tink.backend.aggregation.agents.nxgen.it.creditcards.amex;

public final class AmericanExpressITConstants {

    private AmericanExpressITConstants() {
        throw new AssertionError();
    }

    public static final String MARKET = "IT";
    public static final String PROVIDER_NAME = "it-americanexpress-password";

    public static class HeaderValues {
        public static final String APP_ID = "it.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20IT/12 CFNetwork/978.0.7 Darwin/18.7.0";
        public static final String LOCALE = "it_IT";
        public static final String APP_VERSION = "6.24.0";
        public static final String GIT_SHA = "3c9712664";
    }
}
