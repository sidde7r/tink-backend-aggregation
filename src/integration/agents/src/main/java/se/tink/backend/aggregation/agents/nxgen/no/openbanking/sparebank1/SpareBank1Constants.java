package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1;

public abstract class SpareBank1Constants {

    public static class Urls {
        public static final String BASE_URL = "https://developer-api.sparebank1.no";
        public static final String GET_TOKEN = BASE_URL + "/oauth/token";
        public static final String GET_ACCOUNTS = BASE_URL + "/open/personal/banking/accounts/all";
        public static final String GET_TRANSACTIONS =
                BASE_URL + "/open/personal/banking/accounts/{accountId}/transactions";
    }

    public static class HeaderKeys {
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        public static final String CACHE_CONTROL = "no-cache";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class StorageKeys {
        public static final String TOKEN = "token";
        public static final String ACCOUNT_ID = "account_id";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "sparebank1";
    }
}
