package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

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

        public static final String BASE_API_URL = "https://api.abanca.com/sandbox/psd2";
        public static final String BASE_AUTH_URL = "https://api.abanca.com/oauth2";

        public static final URL TOKEN = new URL(BASE_AUTH_URL + Endpoints.TOKEN);

        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
        public static final URL BALANCE = new URL(BASE_API_URL + Endpoints.BALANCE);
    }

    public static class Endpoints {
        public static final String ACCOUNTS = "/me/accounts";
        public static final String TRANSACTIONS = "/me/accounts/{accountId}/transactions";
        public static final String TOKEN = "/token";
        public static final String BALANCE = "/me/accounts/{accountId}/balance";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class HeaderKeys {
        public static final String AUTH_KEY = "authkey";
        public static final String AUTHORIZATION = "authorization";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String INVALID_BALANCE_RESPONSE = "Invalid balance response";
    }

    public class UrlParameters {

        public static final String ACCOUNT_ID = "accountId";
    }

    public class QueryKeys {

        public static final String APPLICATION = "aplicacion";
        public static final String GRANT_TYPE = "grant_type";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String API_KEY = "api_key";
    }

    public static class HeaderValues {

        public static final String TOKEN_PREFIX = "Bearer ";
    }

    public static class FormValues {

        public static final String APPLICATION = "SANDEX0001";
        public static final String GRANT_TYPE = "password";
    }
}
