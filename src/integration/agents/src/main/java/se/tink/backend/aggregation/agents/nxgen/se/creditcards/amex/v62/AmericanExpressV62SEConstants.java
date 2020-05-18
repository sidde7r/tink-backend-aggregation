package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62;

public final class AmericanExpressV62SEConstants {

    public static final String MARKET = "SE";
    public static final String PROVIDER_NAME = "se-americanexpress-password";
    public static final String INIT_VERSION =
            "726b052bcadfae880a9e1b3b29b0adbd0226124cE3_PRODbbd1552d-41b6-4f83-8026-872e6a72797a";

    private AmericanExpressV62SEConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "se.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20SE/6 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "sv_SE";
        public static final String APP_VERSION = "6.29.0";
    }
}
