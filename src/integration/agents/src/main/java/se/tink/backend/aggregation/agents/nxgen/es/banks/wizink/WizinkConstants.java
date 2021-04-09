package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

public class WizinkConstants {

    private WizinkConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        private Urls() {}

        private static final String BASE = "https://app.wizink.es/populare-app/webapi";
        public static final String LOGIN = BASE + "/customerLogin";
        public static final String CARD_DETAIL = BASE + "/cardDetail/summary";
        public static final String CARD_DETAIL_TRANSACTIONS = BASE + "/find/movements";
        public static final String TRANSACTIONS = BASE + "/transactions/search";
        public static final String KEEP_ALIVE = BASE + "/inbox";
        public static final String LOGOUT = BASE + "/customerLogout";
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String DEVICE_ID = "deviceId";
        public static final String LOGIN_TYPE = "loginType";
        public static final String CONNECTION_TYPE = "connectionType";
        public static final String OPERATIVE_SYSTEM = "operativeSystem";
        public static final String MACHINE_TYPE = "machineType";
        public static final String INDIGITALL_DEVICE = "indigitallDevice";
        public static final String X_TOKEN_ID = "X-token-id";
        public static final String X_TOKEN_USER = "X-user-token";
    }

    public static class ErrorCodes {
        private ErrorCodes() {}

        public static final String WRONG_OTP = "807";
    }

    public static class StorageKeys {
        private StorageKeys() {}

        public static final String CARDS_LIST = "creditCardsList";
        public static final String ACCOUNTS_LIST = "accountsList";
        public static final String X_TOKEN_ID = "xTokenId";
        public static final String X_TOKEN_USER = "xTokenUser";
        public static final String FIRST_FULL_REFRESH = "firstFullRefresh";
        public static final String ENCODED_ACCOUNT_NUMBER = "encodedAccountNumber";
    }
}
