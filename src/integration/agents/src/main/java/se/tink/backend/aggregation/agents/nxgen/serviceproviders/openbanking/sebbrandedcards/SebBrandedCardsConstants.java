package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

public abstract class SebBrandedCardsConstants {

    public static class Urls {
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/ais/v1/identified2";

        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/branded-card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/branded-card-accounts//{accountId}/transactions";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "sebopenbanking";
        public static final String CLIENT_NAME = "tink";
    }
}
