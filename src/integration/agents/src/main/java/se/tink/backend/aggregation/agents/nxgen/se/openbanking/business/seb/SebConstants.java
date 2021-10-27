package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;

public class SebConstants {

    public static class Urls {

        public static final String PROVIDER_MARKET = "SE";

        public static final String BASE_AIS = "/tpp/ais/v7/identified2";

        // Urls for Checking accounts
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";
        public static final String BASE_TRANSACTION_DETAILS =
                SebCommonConstants.Urls.BASE_URL + BASE_AIS;

        // Urls for Credit cards
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/tpp/ais/v2/identified2";
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";
    }

    public static class Storage {
        public static final String CREDIT_CARD_TRANSACTION_RESPONSE =
                "creditCardTransactionResponse";
    }
}
