package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SBABConstants {

    public static final String INTEGRATION_NAME = "sbab";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.SAVINGS, "savings").build();

    private SBABConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String UNKNOWN_ACCOUNT_TYPE = "Unknown account type.";
    }

    public static class Urls {

        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);

        public static final URL AUTHORIZATION = new URL(Endpoints.BASE_URL + Endpoints.OAUTH);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.TRANSFERS);
        public static final URL CUSTOMERS = new URL(Endpoints.BASE_URL + Endpoints.CUSTOMERS);
        public static final URL TOKEN = new URL(Endpoints.BASE_URL + Endpoints.TOKEN);
    }

    public static class Endpoints {
        public static final String BASE_URL = "https://developer.sbab.se";
        public static final String OAUTH = "/sandbox/auth/1.0/authorize";
        public static final String TRANSFERS =
                "/sandbox/savings/2.0/accounts/{accountNumber}/transfers";
        public static final String CUSTOMERS = "/sandbox/savings/1.0/customers";
        public static final String ACCOUNTS = "/sandbox/savings/2.0/accounts";
        public static final String TOKEN = "/sandbox/auth/1.0/token";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String TOKEN = "TOKEN";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "pending_code";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "pending_code";
        public static final String SCOPE = "account.read";
        public static final String GRANT_TYPE = "pending_authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class FormKeys {}

    public static class FormValues {}

    public static class LogTags {}

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
    }

    public static class CredentialKeys {
        public static final String USERNAME = "USERNAME";
        public static final String PASSWORD = "PASSWORD";
    }
}
