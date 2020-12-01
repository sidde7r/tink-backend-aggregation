package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class VolvoFinansConstants {

    private VolvoFinansConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String NINETY_DAYS_TRANSACTIONS_ONLY =
                "only allowed to view last 90 days";
    }

    public static class Urls {
        public static final URL AUTH = new URL(Endpoints.BASE_AUTH + Endpoints.AUTH);
        public static final URL TOKEN = new URL(Endpoints.BASE_AUTH + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String BASE_AUTH = "https://auth.volvofinans.se/token/openbanking";
        public static final String BASE_URL = "https://openbanking.api.volvofinans.se";
        public static final String AUTH = "/authorize";
        public static final String TOKEN = "/token";
        public static final String ACCOUNTS = "/openbanking/v1/accounts";
        public static final String TRANSACTIONS = ACCOUNTS + "/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String BEARER = "bearer";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_TO = "dateTo";
        public static final String DATE_FROM = "dateFrom";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "openbanking";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_API_KEY = "X-API-KEY";
    }

    public static class FormKeys {
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class Accounts {
        public static final String STATUS_ENABLED = "enabled";
        public static final String STATUS_EXPECTED = "expected";
    }

    public static class RetryFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
    }
}
