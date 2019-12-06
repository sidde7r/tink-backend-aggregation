package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public final class OpBankConstants {

    public static final String INTEGRATION_NAME = "opbank";

    private OpBankConstants() {
        throw new AssertionError();
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "Käyttötili", "CURRENT ACCOUNT")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static String BASE_URL = "https://mtls.apis.op.fi";
        public static final String OAUTH_TOKEN = BASE_URL + "/oauth/token";
        public static final String ACCOUNTS_AUTHORIZATION =
                BASE_URL + "/accounts-psd2/v1/authorizations";
        public static final String GET_ACCOUNTS = BASE_URL + "/accounts-psd2/v1/accounts";
        public static final String GET_TRANSACTIONS = GET_ACCOUNTS + "/{accountId}/transactions";

        public static final String AUTHORIZATION_URL = "https://authorize.op.fi/oauth/authorize";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class HeaderKeys {
        public static final String X_API_KEY = "x-api-key";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_CUSTOMER_USER_AGENT = "x-customer-user-agent";
        public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    public static class HeaderValues {
        public static final String TINK = "tink";
        public static final String CUSTOMER_IP_ADRESS = "127.0.0.1";
    }

    public static class TokenValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "openid accounts";
        public static final int MAX_AGE = 86400;
    }

    public static class AuthorizationKeys {
        public static final String REQUEST = "request";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
    }

    public static class AuthorizationValues {
        public static final String CODE = "code";
        public static final String OPENID_ACCOUNTS = "openid accounts";
    }

    public static class RefreshTokenFormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String OAUTH2_ACCESS_TOKEN = "oauth2_access_token";
        public static final int DEFAULT_TOKEN_LIFETIME = 45;
        public static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;
    }
}
