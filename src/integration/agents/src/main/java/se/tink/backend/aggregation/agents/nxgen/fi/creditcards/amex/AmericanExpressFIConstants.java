package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

public final class AmericanExpressFIConstants {

    public static final String MARKET = "FI";
    public static final String PROVIDER_NAME = "fi-americanexpress-password";
    public static final String INIT_VERSION =
            "fbf0e7bb1eaa152d46b77477f7a1b4facb11694bE3_PRODbbd1552d-41b6-4f83-8026-872e6a72797a";

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
