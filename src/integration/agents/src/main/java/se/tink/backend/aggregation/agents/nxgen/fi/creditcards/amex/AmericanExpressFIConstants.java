package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

public final class AmericanExpressFIConstants {

    private AmericanExpressFIConstants() {
        throw new AssertionError();
    }

    public static class HeaderValues {
        public static final String APP_ID = "fi.co.americanexpress.amexservice";
        public static final String USER_AGENT = "Amex%20FI/15 CFNetwork/811.5.4 Darwin/16.7.0";
    }

    public static class BodyValues {
        public static final String LOCALE = "fi_FI";
    }
}
