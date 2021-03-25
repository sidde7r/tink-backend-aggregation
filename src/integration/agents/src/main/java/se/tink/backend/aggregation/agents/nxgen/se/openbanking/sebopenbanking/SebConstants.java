package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

public abstract class SebConstants {
    public static final String MARKET = "SE";

    public static class Urls {
        public static final String BASE_AIS = "/tpp/ais/v7/identified2";

        // Urls for Checking accounts
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String TRANSACTIONS = BASE_AIS + "/accounts/{accountId}/transactions";

        // Urls for Credit cards
        private static final String BASE_CREDIT_CARD_ACCOUNTS = "/tpp/ais/v2/identified2";
        public static final String CREDIT_CARD_ACCOUNTS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts";
        public static final String CREDIT_CARD_TRANSACTIONS =
                BASE_CREDIT_CARD_ACCOUNTS + "/card-accounts/{accountId}/transactions";

        // Url for Transfer Destinations
        public static final String ACCOUNT_DETAILS = BASE_AIS + "/accounts/{accountId}";

        // Url for PIS
        private static final String BASE_PIS = "/tpp/pis/v6/identified2/payments";
        public static final String CREATE_PAYMENT = BASE_PIS + "/{paymentProduct}";
        public static final String GET_PAYMENT = BASE_PIS + "/{paymentProduct}/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                BASE_PIS + "/{paymentProduct}/{paymentId}/status";
        public static final String SIGN_PAYMENT =
                BASE_PIS + "/{paymentProduct}/{paymentId}/authorisations";
    }

    public static class ErrorMessages {
        public static final String UNKNOWN_PAYMENT_PRODUCT =
                "The payment product could not be determined";
        public static final String CROSS_BORDER_PAYMENT_NOT_SUPPORTED =
                "Cross border payment is still not supported";
        public static final String PAYMENT_REF_TOO_LONG =
                "Supplied payment reference is too long, max is %s characters.";
        public static final String DATE_TOO_CLOSE_ERROR_MESSAGE =
                "The date when the money will reach the recipient is too close.";
        public static final String NOT_BUSINESS_DAY_ERROR_MESSAGE =
                "The date when the money must reach the recipient must be a banking day";
        public static final String SIMILAR_PAYMENT_ERROR_MESSAGE =
                "A similar payment is already in upcoming events.";
        public static final String PAYMENT_SERVICE_UNAVAILABLE = "Service unavailable.";
        public static final String UNSTRUCTURED_REMITTANCE_INFO_REQUIRED =
                "Bank Giro account does not accept OCR number, you have to enter message instead.";
    }

    public static class IdTags {
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class FormValues {
        public static final String MOBILT_BANK_ID = "mobiltbankid";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class Storage {
        public static final String CREDIT_CARD_TRANSACTION_RESPONSE =
                "creditCardTransactionResponse";
    }

    public static class PaymentProduct {
        public static final String SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS =
                "swedish-domestic-private-bankgiros";
        public static final String SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS =
                "swedish-domestic-private-plusgiros";
        public static final String SEPA_CREDIT_TRANSFER = "sepa-credit-transfers";
        public static final String SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS =
                "swedish-domestic-private-credit-transfers";
    }

    public static class PaymentValue {
        public static final int MAX_DEST_MSG_LEN = 12;
    }

    public static class AccountProductTypes {

        public static final String PRIVAT_KONTO = "privatkonto";
        public static final String ENKLA_SPARKONTO = "enkla sparkontot";
        public static final String PERSONALLONEKONTO = "personallönekonto";
        public static final String VALUTAKONTO = "valutakonto";
        public static final String NOTARIATKONTO = "notariatkonto";
    }
}
