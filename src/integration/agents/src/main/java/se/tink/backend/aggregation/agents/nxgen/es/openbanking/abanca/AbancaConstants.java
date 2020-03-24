package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class AbancaConstants {

    private AbancaConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    public static final String INTEGRATION_NAME = "abanca";

    public static class Urls {

        public static final String BASE_API_URL = "https://api.abanca.com/psd2";
        public static final String BASE_AUTH_URL = "https://api.abanca.com";

        public static final URL AUTHORIZATION = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZATION);
        public static final URL TOKEN = new URL(BASE_AUTH_URL + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
        public static final URL BALANCE = new URL(BASE_API_URL + Endpoints.BALANCE);
    }

    public static class Endpoints {
        public static final String AUTHORIZATION = "/oauth/{clientId}/Abanca";
        public static final String ACCOUNTS = "/me/accounts";
        public static final String TRANSACTIONS = "/me/accounts/{accountId}/transactions";
        public static final String TOKEN = "/oauth2/token";
        public static final String BALANCE = "/me/accounts/{accountId}/balance";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class HeaderKeys {
        public static final String AUTH_KEY = "AuthKey";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String INVALID_BALANCE_RESPONSE = "Invalid balance response";
    }

    public class UrlParameters {

        public static final String ACCOUNT_ID = "accountId";
        public static final String CLIENT_ID = "clientId";
    }

    public class QueryKeys {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String API_KEY = "api_key";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String CODE = "code";
    }

    public class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "CODE";
        public static final String GRANT_TYPE = "grant_type";
    }

    public static class HeaderValues {

        public static final String TOKEN_PREFIX = "Bearer ";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String APPLICATION = "APLICACION";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String GRANT_TYPE_CODE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH = "refresh_token";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "abanca";
        public static final String CLIENT_NAME = "tink";
    }
}
