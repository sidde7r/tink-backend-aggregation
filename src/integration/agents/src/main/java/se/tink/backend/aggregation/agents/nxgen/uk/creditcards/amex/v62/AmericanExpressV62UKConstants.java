package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

public final class AmericanExpressV62UKConstants {

    public static final String MARKET = "UK";
    public static final String PROVIDER_NAME = "uk-americanexpress-password";
    public static final String INIT_VERSION =
            "27053fea46603dea320db37de22db9b47145db9aE3_PRODecd4b2f8-80e1-444f-9316-ddc6d5f16218";

    private AmericanExpressV62UKConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "uk.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20UK/24 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String LOCALE = "en_GB";
        public static final String APP_VERSION = "6.30.0";
    }
}
