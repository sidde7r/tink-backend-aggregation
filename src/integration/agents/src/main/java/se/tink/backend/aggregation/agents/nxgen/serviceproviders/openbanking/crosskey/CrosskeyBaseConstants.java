package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

public abstract class CrosskeyBaseConstants {

    public static class Urls {
        public static final String TOKEN = "/oidc/v1.0/token";
        public static final String OAUTH = "/oidc/auth";
        public static final String ACCOUNT_ACCESS_CONSENTS =
                "/open-banking/v3.1/aisp/account-access-consents";
        public static final String ACCOUNT_ACCESS_CONSENT =
                "/open-banking/v3.1/aisp/account-access-consents/{consentId}";
        public static final String ACCOUNTS = "/open-banking/v3.1/aisp/accounts";
        public static final String ACCOUNT_BALANCES =
                "/open-banking/v3.1/aisp/accounts/{accountId}/balances";
        public static final String ACCOUNT_TRANSACTIONS =
                "/open-banking/v3.1/aisp/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String TOKEN = "TOKEN";
        public static final String CONSENT = "CONSENT";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String PAGINATION = "pageKey";
        public static final String NONCE = "nonce";
        public static final String REQUEST = "request";
        public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
        public static final String CLIENT_ASSERTION = "client_assertion";
        public static final String FROM_BOOKING_DATE_TIME = "fromBookingDateTime";
        public static final String TO_BOOKING_DATE_TIME = "toBookingDateTime";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String RESPONSE_TYPE = "code id_token";
        public static final String SCOPE = "openid accounts";
        public static final String SCOPE_OPENID = "openid";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CLIENT_ASSERTION_TYPE =
                "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    public static class HeaderKeys {
        public static final String X_API_KEY = "X-API-Key";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL =
                "Invalid Config: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_BALANCE = "No balance found";
        public static final String MISSING_TOKEN = "Cannot find token!";
        public static final String MISSING_CONSENT = "Consent has to be acquired first";
    }

    public static class OIDCValues {
        public static final String SCOPE = "openid accounts";
        public static final String ALG = "RS256";
        public static final String TYP = "JWT";
        public static final String TOKEN_ID_PREFIX = "urn:crosskey:account:";
        public static final String[] CONSENT_PERMISSIONS = {
            "ReadAccountsDetail",
            "ReadBalances",
            "ReadTransactionsDetail",
            "ReadTransactionsCredits",
            "ReadTransactionsDebits"
        };
    }

    public static class UrlParameters {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class AccountType {
        public static final String CREDIT_CARD = "CreditCard";
        public static final String CURRENT_ACCOUNT = "CurrentAccount";
    }

    public static class AccountBalanceType {
        public static final String BOOKED = "InterimBooked";
        public static final String AVAILABLE = "InterimAvailable";
    }

    public static class Transactions {
        public static final String STATUS_BOOKED = "Booked";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        public static final String TRANSACTION_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }

    public class Encryption {
        public static final String KEY_ALGORITHM = "RSA";
    }
}
