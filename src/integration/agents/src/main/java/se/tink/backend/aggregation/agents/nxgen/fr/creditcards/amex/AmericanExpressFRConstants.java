package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

public final class AmericanExpressFRConstants {

    public static final String MARKET = "FR";
    public static final String PROVIDER_NAME = "fr-americanexpress-password";

    private AmericanExpressFRConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "fr.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FR/6.21.1 CFNetwork/808.4.18 Darwin/16.5.0";
    }

    public static class BodyValues {
        public static final String LOCALE = "fr_FR";
        public static final String CLIENT_VERSION = "6.21.1";
    }
}
