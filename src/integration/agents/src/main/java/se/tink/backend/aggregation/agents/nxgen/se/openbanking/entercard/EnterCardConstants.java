package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class EnterCardConstants {

    public static final String INTEGRATION_NAME = "entercard";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.LOAN, "loan")
                    .put(AccountTypes.CREDIT_CARD, "card")
                    .build();

    private EnterCardConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final URL AUTHORIZATION =
                new URL(Endpoints.BASE_URL_AUTH + Endpoints.AUTHORIZE);
        public static final URL TOKEN = new URL(Endpoints.BASE_URL_AUTH + Endpoints.TOKEN);
        public static final URL ACCOUNTS =
                new URL(Endpoints.BASE_URL_ACCOUNTS + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS =
                new URL(Endpoints.BASE_URL_TRANSACTIONS + Endpoints.MOVEMENTS);
    }

    public static class Endpoints {
        public static final String BASE_URL_AUTH =
                "https://private-anon-98c552962a-authenticationandauthorizationapi.apiary-mock.com";
        public static final String BASE_URL_ACCOUNTS =
                "https://private-anon-24f9c0b723-accountdetailsapi.apiary-mock.com";
        public static final String AUTHORIZE = "/v1/authorize";
        public static final String TOKEN = "/v1/token";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BASE_URL_TRANSACTIONS =
                "https://private-anon-1b525c52f4-movements.apiary-mock.com";
        public static final String MOVEMENTS = "/v1/movements";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String AUTH_CODE = "AUTHORIZATION_CODE";
        public static final String TOKEN = "TOKEN";
        public static final String ACCOUNT_NUMBER = "ACCOUNT_NUMBER";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grantType";
        public static final String REFRESH_TOKEN = "refreshToken";
        public static final String REDIRECT_URI = "redirectUri";
        public static final String CODE = "code";
        public static final String CLIENT_ID = "clientId";
        public static final String RESPONSE_TYPE = "responseType";
        public static final String SCOPE = "scope";
        public static final String CODE_CHALLENGE = "codeChallenge";
        public static final String CODE_CHALLENGE_METHOD = "codeChallengeMethod";
        public static final String STATE = "state";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String CODE_VERIFIER = "codeVerifier";
        public static final String FROM_BOOKING_DATE_TIME = "fromDateTime";
        public static final String TO_BOOKING_DATE_TIME = "toDateTime";
        public static final String ACCOUNT_NUMBER = "accountNumber";
    }

    public static class QueryValues {
        public static final String SCOPE = "scope1";
        public static final String RESPONSE_TYPE = "code";
        public static final String S256 = "S256";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CLIENT_ID = "api-key";
    }

    public static class FormKeys {
        public static final String RESPONSE_TYPE = "responseType";
        public static final String CLIENT_ID = "clientId";
    }

    public static class FormValues {
        public static final String RESPONSE_TYPE = "responseType";
    }

    public static class LogTags {}

    public static class AccountType {
        public static final String CREDIT_CARD = "card";
    }

    public static class Transactions {
        public static final String OPEN = "OPEN";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    }
}
