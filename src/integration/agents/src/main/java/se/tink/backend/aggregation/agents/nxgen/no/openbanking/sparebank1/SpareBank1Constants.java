package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SpareBank1Constants {

    public static final String INTEGRATION_NAME = "sparebank1";

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "USER").build();

    private SpareBank1Constants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_URL = "https://developer-api.sparebank1.no/";

        public static final URL FETCH_TOKEN = new URL(BASE_URL + ApiService.GET_TOKEN);
        public static final URL FETCH_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL FETCH_TRANSACTIONS =
                new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
    }

    public static class ApiService {
        public static final String GET_TOKEN = "oauth/token";
        public static final String GET_ACCOUNTS = "open/personal/banking/accounts/all";
        public static final String GET_TRANSACTIONS =
                "open/personal/banking/accounts/{accountId}/transactions";
    }

    public static class HeaderKeys {
        public static final String CACHE_CONTROL = "Cache-Control";
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
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String ACCOUNT_ID = "account_id";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }
}
