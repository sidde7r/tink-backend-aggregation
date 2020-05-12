package se.tink.backend.aggregation.agents.nxgen.be.creditcards.amex;

public final class AmericanExpressBEConstants {

    public static final String MARKET = "BE";
    public static final String PROVIDER_NAME = "be-americanexpress-password";

    private AmericanExpressBEConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "be.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20BELUX/6 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "fr_BE";
        public static final String APP_VERSION = "6.29.0";
    }
}
