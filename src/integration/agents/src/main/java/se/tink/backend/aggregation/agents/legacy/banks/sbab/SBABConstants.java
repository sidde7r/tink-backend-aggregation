package se.tink.backend.aggregation.agents.banks.sbab;

public class SBABConstants {
    public static final String INTEGRATION_NAME = "sbab";
    public static final String IS_MORTGAGE_SWITCH_PROVIDER_TEST = "isSwitchMortgageProviderTest";
    public static final String TRUE = "true";

    public static class Url {
        private static final String HOST = "https://secure.sbab.se/";
        private static final String TRANSFER_BASE_ENDPOINT = HOST + "api/transfer-facade-service/";

        public static final String SAVED_RECIPIENTS = TRANSFER_BASE_ENDPOINT + "saved-recipients";
        public static final String TRANSFER_ACCOUNTS = TRANSFER_BASE_ENDPOINT + "accounts";
        public static final String VALIDATE_TRANSFER =
                TRANSFER_BASE_ENDPOINT + "transfers/validate";
        public static final String CONFIRM_TRANSFER = TRANSFER_BASE_ENDPOINT + "transfers";
        public static final String VALIDATE_RECIPIENT =
                TRANSFER_BASE_ENDPOINT + "saved-recipients/validate";
        public static final String INIT_SIGN_PROCESS = TRANSFER_BASE_ENDPOINT + "signature/process";
        public static final String SIGN_TRANSFER = TRANSFER_BASE_ENDPOINT + "signature/";
    }

    public static class TransferStatus {
        public static final String PENDING = "PENDING";
        public static final String COMPLETE = "COMPLETE";
        public static final String UNKNOWN = "UNKNOWN";
    }

    public static class ErrorCode {
        public static final int INVALID_ACCOUNT_NUMBER = 1200;
        public static final int INVALID_ACCOUNT_NUMBER_LEN = 2001;
        public static final int INVALID_TRANSACTION_DATE = 2006;
        public static final int EXCESS_TRANSFER_AMOUNT = 2002;
    }

    public static class BankId {
        public static final int BANKID_MAX_ATTEMPTS = 90;
        public static final String BANKID_AUTH_METHOD = "MOBILE_BANK_ID";

        public static final String CANCELED = "CANCELED";
        public static final String STARTED = "STARTED";
        public static final String SUCCESS = "SUCCESS";
        public static final String ALREADY_IN_PROGRESS = "SIGN_ALREADY_IN_PROGRESS";
    }
}
