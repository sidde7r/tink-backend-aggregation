package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

public abstract class SebConstants {

    public static class Urls {

        public static final String BASE_AUTH_URL = "https://id.seb.se/tpp";
        public static final String BASE_AIS = "/tpp/ais/v5/identified2";
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/tpp/ais/v2/identified2";

        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "seb";
        public static final String CLIENT_NAME = "tink";
    }
}
