package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex.v62;

public final class AmericanExpressV62ATConstants {

    public static final String MARKET = "AT";
    public static final String PROVIDER_NAME = "at-americanexpress-password";
    public static final String INIT_VERSION =
            "6670e0b7b9d4e22eae63b37055e058115c179a7aE3_PRODbbd1552d-41b6-4f83-8026-872e6a72797a";

    private AmericanExpressV62ATConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "at.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20AT/24 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "de_AT";
        public static final String APP_VERSION = "6.30.0";
        public static final String GIT_SHA = "1d0500091";
    }
}
