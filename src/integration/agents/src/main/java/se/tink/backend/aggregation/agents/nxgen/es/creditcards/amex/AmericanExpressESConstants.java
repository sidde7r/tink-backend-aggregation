package se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex;

public final class AmericanExpressESConstants {

    private AmericanExpressESConstants() {
        throw new AssertionError();
    }

    public static final String MARKET = "ES";
    public static final String PROVIDER_NAME = "es-americanexpress-password";

    public static class HeaderValues {
        public static final String APP_ID = "es.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20ES/6.10.0 CFNetwork/808.2.16 Darwin/16.3.0";
        public static final String FACE = "es_ES";
    }

    public static class BodyValue {
        public static final String LOCALE = "es_ES";
        public static final String CLIENT_VERSION = "6.10.0";
    }
}
