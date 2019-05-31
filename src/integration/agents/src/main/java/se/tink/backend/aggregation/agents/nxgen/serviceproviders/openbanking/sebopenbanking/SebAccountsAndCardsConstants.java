package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking;

public abstract class SebAccountsAndCardsConstants {

    public static class Urls {

        private static final String BASE_AIS = "/ais/v5/identified2";
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/ais/v2/identified2";

        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";
        public static final String TRANSACTIONS_NEXT_PAGE_URL_PREFIX = BASE_AIS;
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "sebopenbanking";
        public static final String CLIENT_NAME = "tink";
    }
}
