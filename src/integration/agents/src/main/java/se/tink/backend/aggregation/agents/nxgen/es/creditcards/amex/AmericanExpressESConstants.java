package se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex;

public final class AmericanExpressESConstants {

    private AmericanExpressESConstants() {
        throw new AssertionError();
    }

    public static final String MARKET = "ES";
    public static final String PROVIDER_NAME = "es-americanexpress-password";
    public static final String INIT_VERSION =
            "05ae6eaa7a853cb81affc87ddb82d12928c12fbbE3_PRODbbd1552d-41b6-4f83-8026-872e6a72797a";

    public static class HeaderValues {
        public static final String APP_ID = "es.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20ES/6 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "es_ES";
        public static final String APP_VERSION = "6.29.0";
    }
}
