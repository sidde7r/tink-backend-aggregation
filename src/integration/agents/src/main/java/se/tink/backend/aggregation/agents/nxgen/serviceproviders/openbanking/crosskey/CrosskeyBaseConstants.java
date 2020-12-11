package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

public abstract class CrosskeyBaseConstants {

    public static class Urls {

        public static final String TOKEN = "/oidc/v1.0/token";
        public static final String OAUTH = "/oidc/auth";
        public static final String ACCOUNT_ACCESS_CONSENTS =
                "/open-banking/v3.1/aisp/account-access-consents";
        public static final String ACCOUNTS = "/open-banking/v3.1/aisp/accounts";
        public static final String ACCOUNT_BALANCES =
                "/open-banking/v3.1/aisp/accounts/{accountId}/balances";
        public static final String ACCOUNT_TRANSACTIONS =
                "/open-banking/v3.1/aisp/accounts/{accountId}/transactions";
        public static final String PAYMENT_ACCESS_CONSENTS =
                "/open-banking/v3.1/pisp/international-payment-consents";
        public static final String MAKE_PAYMENT = "/open-banking/v3.1/pisp/international-payments";
        public static final String FETCH_PAYMENT =
                "/open-banking/v3.1/pisp/international-payments/{internationalPaymentId}";
    }

    public static class StorageKeys {
        public static final String TOKEN = "OAUTH_TOKEN";
        public static final String CONSENT = "CONSENT";
        public static final String INTERNATIONAL_ID = "internationalId";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String NONCE = "nonce";
        public static final String REQUEST = "request";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String RESPONSE_TYPE = "code id_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
    }

    public static class HeaderKeys {
        public static final String X_API_KEY = "X-API-Key";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String ACCEPT = "Accept";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
        public static final String X_JWS_SIGNATURE = "x-jws-signature";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_BALANCE = "No balance found";
        public static final String MISSING_TOKEN = "Cannot find token!";
        public static final String CONSENT_ID_NOT_FOUND =
                "Consent Id not found in session storage.";
        public static final String NOT_AUTHENTICATED = "User is not authenticated.";
        public static final String NOT_AUTHORIZED = "User is not authorized.";
    }

    public static class OIDCValues {
        public static final String ALG = "RS256";
        public static final String TYP = "JWT";
        public static final String TOKEN_ID_PREFIX_ACCOUNT = "urn:crosskey:account:";
        public static final String TOKEN_ID_PREFIX_PAYMENT = "urn:crosskey:payment:";
        public static final String SCOPE_OPEN_ID = "openid";
        public static final String SCOPE_PAYMENTS = SCOPE_OPEN_ID + " payments";
        public static final String SCOPE_ACCOUNTS = SCOPE_OPEN_ID + " accounts";
        public static final String SCOPE_ALL = SCOPE_OPEN_ID + " accounts payments";
        public static final String[] CONSENT_PERMISSIONS = {
            "ReadAccountsDetail",
            "ReadBalances",
            "ReadTransactionsDetail",
            "ReadTransactionsCredits",
            "ReadTransactionsDebits",
        };
        public static final String B_64_STR = "b64";
        public static final Boolean B_64 = Boolean.FALSE;
        public static final String IAT = "http://openbanking.org.uk/iat";
        public static final String ISS = "http://openbanking.org.uk/iss";
        public static final String TAN = "http://openbanking.org.uk/tan";
    }

    public static class UrlParameters {
        public static final String ACCOUNT_ID = "accountId";
        public static final String INTERNATIONAL_PAYMENT_ID = "internationalPaymentId";
        public static final String FROM_BOOKING_DATE = "fromBookingDateTime";
        public static final String TO_BOOKING_DATE = "toBookingDateTime";
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
        public static final String DEBIT = "Debit";
        public static final int MINUTES_MARGIN = 10;
        public static final int DAYS_WINDOW = 90;
    }

    public static class Format {
        public static final String TRANSACTION_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ssX";
        public static final String TRANSACTION_DATE_FETCHER = "yyyy-MM-dd'T'HH:mm:ss";
    }

    public static class ExceptionMessagePatterns {
        public static final String UNRECOGNIZED_ACCOUNT_TYPE =
                "Unrecognized Crosskey account type %s";
        public static final String CANNOT_MAP_TINK_ACCOUNT =
                "Cannot map Tink account type : %s to a Crosskey account type.";
        public static final String CANNOT_MAP_CROSSKEY_ACCOUNT =
                "Cannot map Crosskey account type : %s to a Tink account type.";
        public static final String CANNOT_MAP_CROSSKEY_PAYMENT_STATUS =
                "Cannot map Crosskey payment status : %s to Tink payment status.";
        public static final String CANNOT_MAP_TINK_PAYMENT_STATUS =
                "Cannot map Tink payment status : %s to Crosskey payment status.";
    }

    public static class RequestConstants {
        public static final String END_TO_END_IDENTIFICATION = "FRESCO.21302.GFX.20";
    }

    public enum IdentificationType {
        IBAN,
        CREDIT_CARD
    }
}
