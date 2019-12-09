package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject;

public final class OpenBankProjectConstants {

    private OpenBankProjectConstants() {
        throw new UnsupportedOperationException("Cannot instantiate OpenBankProjectConstants");
    }

    public static class Urls {
        public static final String DIRECT_LOGIN_URL = "/my/logins/direct";
        public static final String AIS = "/obp/v3.1.0/banks";
        public static final String ACCOUNTS = AIS + "/{BANK_ID}/accounts";
        public static final String ACCOUNT = AIS + "/{BANK_ID}/accounts/{ACCOUNT_ID}/owner/account";
        public static final String TRANSACTIONS =
                AIS + "/{BANK_ID}/accounts/{ACCOUNT_ID}/{VIEW_ID}/transactions";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String BANK_ID = "BANK_ID";
    }

    public static class ParameterKeys {
        public static final String VIEW_ID = "VIEW_ID";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String BANK_ID = "BANK_ID";
    }

    public static class ParameterValues {
        public static final String OWNER_VIEW_ID = "owner";
    }

    public static class QueryKeys {
        public static final String OFFSET = "offset";
        public static final String SORT_DIRECTION = "sort_direction";
    }

    public static class QueryValues {
        public static final String DESC = "DESC";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "AUTHORIZATION";
        public static final String TOKEN = "token";
        public static final String DIRECT_LOGIN = "DirectLogin token=%s";
    }

    public static class HeaderValues {
        public static final String DIRECT_LOGIN =
                "DirectLogin username=%s, password=%s, consumer_key=%s";
    }

    public static class ErrorMessages {
        public static final String BAD_CONFIGURATION = "OpenBankProject configuration is invalid.";
        public static final String MISSING_CONFIGURATION = "OpenBankProject configuration missing.";
    }

    public static class Fetcher {
        public static final int START_PAGE = 0;
    }
}
