package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62;

public final class AmericanExpressV62SEConstants {

    public static final String MARKET = "SE";
    public static final String PROVIDER_NAME = "se-americanexpress-password";

    private AmericanExpressV62SEConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "se.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20SE/9 CFNetwork/978.0.7 Darwin/18.7.0";
        public static final String LOCALE = "sv_SE";
        public static final String APP_VERSION = "6.29.0";
        public static final String GIT_SHA = "ae88d0e66";
    }
}
