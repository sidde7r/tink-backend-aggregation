package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

public final class AmericanExpressFIConstants {

    private AmericanExpressFIConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "fi.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FI/6 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "fi_FI";
        public static final String APP_VERSION = "6.29.0";
    }
}
