package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.standardchartered;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

public final class StandardCharteredConstants {

    private StandardCharteredConstants() {
        throw new AssertionError();
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Current")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Savings")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        private static final String BASE_AUTH_URL = "https://s2bssotest.standardchartered.com";
        private static final String BASE_API_URL = "https://apitest.standardchartered.com";
        public static final URL CONSENT_FLOW = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZE_URL);
        public static final URL GET_ACCOUNTS = new URL(BASE_API_URL + Endpoints.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS =
                new URL(BASE_API_URL + Endpoints.GET_TRANSACTIONS);
        public static final URL TOKEN = new URL(BASE_API_URL + Endpoints.TOKEN);
    }

    public static class Endpoints {
        public static final String AUTHORIZE_URL =
                "/unifiedlogin/api/login/tpp/consent/validateTPP";
        public static final String GET_ACCOUNTS = "/api/openapi/account/balance";
        public static final String GET_TRANSACTIONS =
                "/api/openapi/account/{accountId}/transactions";
        public static final String TOKEN = "/oauth/tokens";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN =
                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class QueryKeys {
        public static final String TPP_ID = "tpp_id";
        public static final String TPP_TYPE = "tpp_type";
        public static final String STATE = "state";
        public static final String CORRELATION_ID = "correlationId";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }
}
