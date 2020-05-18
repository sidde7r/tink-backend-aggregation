package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

public final class AmericanExpressFRConstants {

    public static final String MARKET = "FR";
    public static final String PROVIDER_NAME = "fr-americanexpress-password";
    public static final String INIT_VERSION =
            "91e94eb35a848a7600b20786c4de8673cf01ed15E3_PROD4c06ed95-ec8f-4156-a9d9-10dc8879e874";

    private AmericanExpressFRConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "fr.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FR/24 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "fr_FR";
        public static final String APP_VERSION = "6.30.0";
    }
}
