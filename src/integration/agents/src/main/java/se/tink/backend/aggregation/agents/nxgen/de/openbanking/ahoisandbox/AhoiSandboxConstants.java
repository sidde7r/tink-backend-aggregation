package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class AhoiSandboxConstants {

    public static final String INTEGRATION_NAME = "ahoisandbox";

    private AhoiSandboxConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "GIRO")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class Urls {

        public static final String BASE_URL = "https://banking-sandbox.starfinanz.de";

        public static final URL OAUTH = new URL(BASE_URL + "/auth/v1/oauth/token");
        public static final URL REGISTRATION = new URL(BASE_URL + "/ahoi/api/v2/registration");
        public static final URL PROVIDERS = new URL(BASE_URL + "/ahoi/api/v2/providers/");
        public static final URL CREATE_ACCESS = new URL(BASE_URL + "/ahoi/api/v2/accesses");
        public static final URL ACCOUNTS =
                new URL(BASE_URL + "/ahoi/api/v2/accesses/{ACCESS_ID}/accounts");
        public static final URL TRANSACTIONS =
                new URL(
                        BASE_URL
                                + "/ahoi/api/v2/accesses/{ACCESS_ID}/accounts/{ACCOUNT_ID}/transactions");
    }

    public static class UrlParameters {
        public static final String ACCESS_ID = "ACCESS_ID";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class StorageKeys {
        public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {

        public static final String GRANT_TYPE = "grant_type";
    }

    public static class QueryValues {

        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class HeaderKeys {

        public static final String AUTHORIZATION = "Authorization";
        public static final String X_AUTHORIZATION_AHOI = "X-Authorization-Ahoi";
    }

    public class HeaderValues {

        public static final String BEARER_PREFIX = "Bearer ";
    }

    public class Forms {

        public static final String ACCESS_TYPE = "BankAccess";
    }
}
