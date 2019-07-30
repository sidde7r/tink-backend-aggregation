package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

public class BuddybankConstants {

    private BuddybankConstants() {
        throw new AssertionError();
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "buddybank-it";
    }

    public static class ErrorMessages {
        public static final String PICKUP_CODE_FAILURE = "Unexpected pickup code message format";
    }
}
