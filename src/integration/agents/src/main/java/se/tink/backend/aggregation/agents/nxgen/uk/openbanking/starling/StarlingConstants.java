package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;

public class StarlingConstants {

    public static final String UKOB_CERT_ID = "UKOB";
    public static final String CLIENT_ID_PARAM_KEY = "client_id";
    public static final String CLIENT_SECRET_PARAM_KEY = "client_secret";

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

        static final String GET_ACCOUNTS = "/api/v2/accounts";
        static final String GET_ACCOUNT_HOLDER = "/api/v2/account-holder";
        static final String GET_INDIVIDUAL_ACCOUNT_HOLDER = "/api/v2/account-holder/individual";
        static final String GET_JOINT_ACCOUNT_HOLDER = "/api/v2/account-holder/joint";
        static final String GET_BUSINESS_ACCOUNT_HOLDER = "/api/v2/account-holder/business";
        static final String GET_SOLE_TRADER_ACCOUNT_HOLDER = "/api/v2/account-holder/sole-trader";
        static final String GET_ACCOUNT_IDENTIFIERS =
                "/api/v2/accounts/{" + UrlParams.ACCOUNT_UID + "}/identifiers";
        static final String GET_ACCOUNT_BALANCE =
                "/api/v2/accounts/{" + UrlParams.ACCOUNT_UID + "}/balance";
        static final String GET_ANY_TRANSACTIONS =
                "/api/v2/feed/account/{"
                        + UrlParams.ACCOUNT_UID
                        + "}/category/{"
                        + UrlParams.CATEGORY_UID
                        + "}/transactions-between";
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
        public static final URL GET_ACCESS_TOKEN =
                new URL("https://token-api.starlingbank.com/oauth/access-token");
        public static final String API_STARLING = "https://api.starlingbank.com";

        public static final URL GET_ACCOUNTS = new URL(API_STARLING + ApiEndpoint.GET_ACCOUNTS);
        public static final URL GET_ACCOUNT_HOLDER =
                new URL(API_STARLING + ApiEndpoint.GET_ACCOUNT_HOLDER);
        public static final URL GET_INDIVIDUAL_ACCOUNT_HOLDER =
                new URL(API_STARLING + ApiEndpoint.GET_INDIVIDUAL_ACCOUNT_HOLDER);
        public static final URL GET_JOINT_ACCOUNT_HOLDER =
                new URL(API_STARLING + ApiEndpoint.GET_JOINT_ACCOUNT_HOLDER);
        public static final URL GET_BUSINESS_ACCOUNT_HOLDER =
                new URL(API_STARLING + ApiEndpoint.GET_BUSINESS_ACCOUNT_HOLDER);
        public static final URL GET_SOLE_TRADER_ACCOUNT_HOLDER =
                new URL(API_STARLING + ApiEndpoint.GET_SOLE_TRADER_ACCOUNT_HOLDER);
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

    public class AccountHolderType {
        public static final String INDIVIDUAL = "INDIVIDUAL";
        public static final String JOINT = "JOINT";
        public static final String BUSINESS = "BUSINESS";
        public static final String SOLE_TRADER = "SOLE_TRADER";
        public static final String BANKING_AS_A_SERVICE = "BANKING_AS_A_SERVICE";
    }

    public class RequestKey {
        public static final String FROM = "minTransactionTimestamp";
        public static final String TO = "maxTransactionTimestamp";
    }

    public class HeaderKey {
        public static final String DIGEST = "Digest";
        public static final String DATE = "DATE";
    }

    public class TransactionDirections {

        public static final String OUT = "OUT";
    }
}
