package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SkandiaConstants {
    public static final String PROVIDER_MARKET = "SE";

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "Allt-i-Ett konto")
                    .put(TransactionalAccountType.SAVINGS, "Sparkonto")
                    .build();

    private SkandiaConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class ErrorCodes {
        public static final String INVALID_GRANT = "invalid_grant";
    }

    public static class Urls {
        public static final String BASE_OAUTH = "https://fsts.skandia.se/as";
        public static final String BASE_URL = "https://apis.skandia.se/open-banking/core-bank";
        public static final URL AUTHORIZE = new URL(BASE_OAUTH + ApiServices.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE_OAUTH + ApiServices.TOKEN);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiServices.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiServices.GET_TRANSACTIONS);
        public static final URL GET_BALANCES = new URL(BASE_URL + ApiServices.GET_BALANCES);
    }

    public static class ApiServices {
        public static final String AUTHORIZE = "/authorization.oauth2";
        public static final String TOKEN = "/token.oauth2";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
        public static final String GET_BALANCES = "/v1/accounts/{accountId}/balances";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class QueryKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String PENDING = "pending";
        public static final String BOOKED = "booked";
        public static final String CODE = "code";
        public static final String SCOPE = "psd2.aisp";
    }

    public static class FormKeys {
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_CLIENT_CERTIFICATE = "X-Client-Certificate";
        public static final String CLIENT_ID = "Client-Id";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }
}
