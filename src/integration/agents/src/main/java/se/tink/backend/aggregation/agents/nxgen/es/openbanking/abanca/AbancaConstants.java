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

        public static final String BASE_URL = "https://api.abanca.com/sandbox";
        public static final URL ACCOUNTS = new URL(BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String ACCOUNTS = "/me/cuentas/contratos";
        public static final String TRANSACTIONS = "/me/cuentas/contratos/{accountId}/movimientos";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class HeaderKeys {
        public static final String AUTH_KEY = "authkey";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public class UrlParameters {

        public static final String ACCOUNT_ID = "accountId";
    }
}
