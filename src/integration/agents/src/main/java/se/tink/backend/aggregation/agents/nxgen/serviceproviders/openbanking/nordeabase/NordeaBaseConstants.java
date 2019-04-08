package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class NordeaBaseConstants {
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "Current")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .build();

    private NordeaBaseConstants() {
        throw new AssertionError();
    }

    public static class Market {
        public static String INTEGRATION_NAME = "nordea";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.nordeaopenbanking.com";
        public static final String AUTHORIZE = BASE_URL + "/v3/authorize";
        public static final String GET_TOKEN = BASE_URL + "/v3/authorize/token";
        public static final String GET_ACCOUNTS = BASE_URL + "/v3/accounts";
        public static final String GET_TRANSACTIONS =
                BASE_URL + "/v3/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String REDIRECT_URI = "redirectUri";
        public static final String ACCOUNT_ID = "account_id";
        public static final String ACCESS_TOKEN = "accessToken";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String X_CLIENT_ID = "X-IBM-Client-Id";
        public static final String X_CLIENT_SECRET = "X-IBM-Client-Secret";
        public static final String STATE = "state";
        public static final String DURATION = "duration";
        public static final String COUNTRY = "country";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
    }

    public static class QueryValues {
        public static final String DURATION = "12000";
        public static final String SCOPE =
                "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,"
                        + "ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS,PAYMENTS_MULTIPLE";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }
}
