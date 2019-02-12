package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;

public class StarlingConstants {

    public static class URL {
        private static final String API_STARLING_COM = "https://api-sandbox.starlingbank.com/";
        public static final String AUTH_STARLING_COM = "https://oauth-sandbox.starlingbank.com/";
        public static final String OAUTH2_TOKEN = API_STARLING_COM + "oauth/access-token";
        public static final String ACCOUNTS = API_STARLING_COM + "/api/v2/accounts";
        public static final String ACCOUNT_HOLDER =
                API_STARLING_COM + "/api/v2/account-holder/individual";
        public static final String ANY_TRANSACTIONS = API_STARLING_COM + "/api/v1/transactions";

        public static final String GET_PAYEES = API_STARLING_COM + "/api/v2/payees";

        private static final String ACCOUNT_IDENTIFIERS =
                API_STARLING_COM + "/api/v2/accounts/%s/identifiers";
        private static final String ACCOUNT_BALANCE =
                API_STARLING_COM + "/api/v2/accounts/%s/balance";
        private static final String EXECUTE_PAYMENT =
                "/api/v2/payments/local/account/%s/category/%s";

        public static String ACCOUNT_IDENTIFIERS(final String accountUid) {
            return String.format(ACCOUNT_IDENTIFIERS, accountUid);
        }

        public static String ACCOUNT_BALANCE(final String accountUid) {
            return String.format(ACCOUNT_BALANCE, accountUid);
        }

        public static String EXECUTE_PAYMENT(final String accountUid, final String categoryUid) {
            return String.format(EXECUTE_PAYMENT, accountUid, categoryUid);
        }
    }

    public class RequestValue {
        public static final String CODE = OAuth2Constants.CallbackParams.CODE;
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public class RequestKey {
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = OAuth2Constants.CallbackParams.CODE;
    }

    public class StorageKey {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URL = "redirect_url";
    }
}
