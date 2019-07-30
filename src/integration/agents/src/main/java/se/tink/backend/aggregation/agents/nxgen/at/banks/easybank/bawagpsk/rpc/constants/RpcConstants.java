package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.constants;

public final class RpcConstants {
    private RpcConstants() {
        throw new AssertionError();
    }

    public static final String CHANNEL = "MOBILE";
    public static final String LANGUAGE = "en";
    public static final String DEV_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    public static final String DEVICE_IDENTIFIER = "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA";

    public static final String BOOKING_DATE = "BOOKING_DATE";
    public static final String TRANSACTION_TYPE = "ALL";

    public static final String IS_INCLUDE_LOGIN_IMAGE = "0";

    public static final String ACTION_CALL = "MailBox/countOfUnreadedEmails";

    public static final int DISPOSER_NUMBER_LENGTH =
            17; // Length when account number is left-padded with zeros

    public static class Client {
        // Values sent in LoginRequest messages originating from the app
        public static final String BANK_CODE = "14000";
    }

    public static class Server {
        // Values sent in LoginResponse messages originating from the bank
        public static final String BANK_CODE = "60000";
        public static final String BIC = "BAWAATWW";
        public static final String CODE = "0110";
        public static final String SHORT_NAME = "PSK";
    }

    public static class Messages {
        public static final String ACCOUNT_LOCKED = "ERR_DISPOSER_DEACTIVATED";
        public static final String INCORRECT_CREDENTIALS = "ERR_LOGIN";
    }
}
