package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

public final class AmericanExpressFIConstants {

    private AmericanExpressFIConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "fi.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FI/18 CFNetwork/978.0.7 Darwin/18.7.0";
        public static final String LOCALE = "fi_FI";
        public static final String APP_VERSION = "6.17.0";
    }
}
