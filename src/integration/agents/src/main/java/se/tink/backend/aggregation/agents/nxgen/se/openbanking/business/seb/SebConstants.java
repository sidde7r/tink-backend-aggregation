package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;

public class SebConstants {
    public static final String MARKET = "SE";

    public static class Urls {
        public static final String BASE_AUTH_URL = "https://id.seb.se/tpp";
        public static final String BASE_AIS = "/tpp/ais/v5/identified2";

        // Urls for Checking accounts
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";
        public static final String BASE_TRANSACTION_DETAILS =
                SebCommonConstants.Urls.BASE_URL + "/tpp/ais/v6/identified2";

        // Urls for Credit cards
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/tpp/ais/v2/identified2";
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String UNKNOWN_PAYMENT_PRODUCT =
                "The payment product could not be determined";
        public static final String CROSS_BORDER_PAYMENT_NOT_SUPPORTED =
                "Cross border payment is still not supported";
    }

    public static class IdTags {
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class FormValues {
        public static final String MOBILT_BANK_ID = "mobiltbankid";
        public static final String CREDITORS_MESSAGE = "Creditor's message";
        public static final String DEBTORS_MESSAGE = "Debtor's messages";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class Storage {
        public static final String CREDIT_CARD_TRANSACTION_RESPONSE =
                "creditCardTransactionResponse";
    }
}
