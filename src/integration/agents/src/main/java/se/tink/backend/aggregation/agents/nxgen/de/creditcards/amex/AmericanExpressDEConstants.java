package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

public final class AmericanExpressDEConstants {

    private AmericanExpressDEConstants() {
        throw new AssertionError();
    }

    public static final String MARKET = "DE";
    public static final String PROVIDER_NAME = "de-americanexpress-password";
    public static final String INIT_VERSION = "04b61391d6eaf9b55b73b2a858b30f5cbc601430E3_PRODnull";

    public static class HeaderValues {
        public static final String APP_ID = "de.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20DE/6 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "de_DE";
        public static final String APP_VERSION = "6.29.0";
        public static final String GIT_SHA = "16df6e311";
    }
}
