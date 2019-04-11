package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.account.AccountIdentifier;

public class StarlingConstants {
    static final String INTEGRATION_NAME = "starling";

    public static final TypeMapper<AccountIdentifier.Type> ACCOUNT_IDENTIFIER_MAPPER =
            TypeMapper.<AccountIdentifier.Type>builder()
                    .put(AccountIdentifier.Type.IBAN, "IBAN")
                    .put(AccountIdentifier.Type.SORT_CODE, "SORT_CODE")
                    .build();

    public static class UrlParams {
        public static final String ACCOUNT_UID = "accountUid";
        public static final String CATEGORY_UID = "categoryUid";
        public static final String PAYMENT_ORDER_UID = "categoryUid";
    }

    private static class ApiEndpoint {

        static final String GET_OAUTH2_TOKEN = "/oauth/access-token";
        static final String GET_ACCOUNTS = "/api/v2/accounts";
        static final String GET_ACCOUNT_HOLDER = "/api/v2/account-holder/individual";
        static final String GET_ACCOUNT_IDENTIFIERS =
                "/api/v2/accounts/{" + UrlParams.ACCOUNT_UID + "}/identifiers";
        static final String GET_ACCOUNT_BALANCE =
                "/api/v2/accounts/{" + UrlParams.ACCOUNT_UID + "}/balance";
        static final String GET_ANY_TRANSACTIONS = "/api/v1/transactions";
        static final String GET_PAYEES = "/api/v2/payees";

        static final String PUT_PAYMENT =
                "/api/v2/payments/local/account/{"
                        + UrlParams.ACCOUNT_UID
                        + "}/category/{"
                        + UrlParams.CATEGORY_UID
                        + "}";

        static final String GET_PAYMENT_STATUS =
                "/api/v2/payments/local/payment-order/{" + UrlParams.PAYMENT_ORDER_UID + "}";
    }

    public static class Url {
        public static final String AUTH_STARLING = "https://oauth.starlingbank.com";
        private static final String API_STARLING = "https://api.starlingbank.com";

        public static final URL GET_OAUTH2_TOKEN =
                new URL(API_STARLING + ApiEndpoint.GET_OAUTH2_TOKEN);
        public static final URL GET_ACCOUNTS = new URL(API_STARLING + ApiEndpoint.GET_ACCOUNTS);
        public static final URL GET_ACCOUNT_HOLDER =
                new URL(API_STARLING + ApiEndpoint.GET_ACCOUNT_HOLDER);
        public static final URL GET_ACCOUNT_IDENTIFIERS =
                new URL(API_STARLING + ApiEndpoint.GET_ACCOUNT_IDENTIFIERS);
        public static final URL GET_ACCOUNT_BALANCE =
                new URL(API_STARLING + ApiEndpoint.GET_ACCOUNT_BALANCE);
        public static final URL GET_ANY_TRANSACTIONS =
                new URL(API_STARLING + ApiEndpoint.GET_ANY_TRANSACTIONS);
        public static final URL GET_PAYEES = new URL(API_STARLING + ApiEndpoint.GET_PAYEES);
        public static final URL PUT_PAYMENT = new URL(API_STARLING + ApiEndpoint.PUT_PAYMENT);
        public static final URL GET_PAYMENT_STATUS =
                new URL(API_STARLING + ApiEndpoint.GET_PAYMENT_STATUS);
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
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = OAuth2Constants.CallbackParams.CODE;
    }

    public class StorageKey {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URL = "redirect_url";
    }

    public class HeaderKey {
        public static final String AUTHORIZATION = "Authorization";
        public static final String DIGEST = "Digest";
        public static final String DATE = "DATE";
    }
}
