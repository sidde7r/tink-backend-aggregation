package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

public abstract class SebConstants {
    public static final String MARKET = "SE";

    public static class Urls {

        public static final String BASE_AUTH_URL = "https://id.seb.se/tpp";
        public static final String BASE_AIS = "/tpp/ais/v5/identified2";
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/tpp/ais/v2/identified2";
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";
        private static final String BASE_PIS = "/tpp/pis/v5/identified2/payments";
        public static final String CREATE_PAYMENT = BASE_PIS + "/{paymentProduct}";
        public static final String GET_PAYMENT = BASE_PIS + "/{paymentProduct}/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                BASE_PIS + "/{paymentProduct}/{paymentId}/status";
        public static final String SIGN_PAYMENT =
                BASE_PIS + "/{paymentProduct}/{paymentId}/authorisations";
    }

    public static class Market {
        public static final String INTEGRATION_NAME = "seb";
        public static final String CLIENT_NAME = "tink";
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
}
