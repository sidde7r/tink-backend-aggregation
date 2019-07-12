package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class VolvoFinansConstants {

    public static final String INTEGRATION_NAME = "volvofinans";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "debit").build();

    private VolvoFinansConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final URL AUTH = new URL(Endpoints.BASE_AUTH + Endpoints.AUTH);
        public static final URL TOKEN = new URL(Endpoints.BASE_AUTH + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String BASE_AUTH = "https://secure.sandbox.volvofinans.se";
        public static final String BASE_URL = "https://api.sandbox.volvofinans.se";
        public static final String AIS = "/openbanking";
        public static final String AUTH = "/oauth/auth";
        public static final String TOKEN = "/oauth/token";
        public static final String ACCOUNTS = "/openbanking/v1/accounts";
        public static final String TRANSACTIONS = ACCOUNTS + "/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String BEARER = "bearer";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_TO = "dateTo";
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "oauth2";
        public static final String SANDBOX = "sandbox";
        public static final Object GRANT_TYPE = "authorization_code";
        public static final String SANDBOX_CLIENT = "openbanking";
        public static final String TRUE = "true";
        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_API_KEY = "X-API-KEY";
    }

    public static class FormKeys {
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_CODE = "refresh_code";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CLIENT_ID = "openbanking";
        public static final String CLIENT_SECRET = "sandbox";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class Accounts {
        public static final String STATUS_ENABLED = "enabled";
        public static final String STATUS_EXPECTED = "expected";
        public static final String SEK = "SEK";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd";
    }
}
