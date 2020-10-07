package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

public class ICSConstants {

    public static final class Urls {
        public static final String AUTH_BASE = "https://auth.vvwpgvhh.icscards.nl";
        public static final String BASE = "https://api.vvwpgvhh.icscards.nl";
        public static final String ACCOUNT_SETUP = "/1/api/open-banking/v1.1/account-requests";
        public static final String ACCOUNT = "/1/api/open-banking/v1.1/accounts";
        public static final String BALANCES = "/1/api/open-banking/v1.1/accounts/%s/balances";
        public static final String TRANSACTIONS =
                "/1/api/open-banking/v1.1/accounts/%s/transactions";
        public static final String OAUTH_AUTHORIZE = "/openbanking-oauth-tokenv1/authorize";
        public static final String OAUTH_TOKEN = "/openbanking-oauth-tokenv1/token";
    }

    public static final class QueryKeys {
        public static final String ACCOUNT_REQUEST_ID = "AccountRequestId";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String AUTH_CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String FROM_BOOKING_DATE = "fromBookingDate";
        public static final String TO_BOOKING_DATE = "toBookingDate";
    }

    public static final class QueryValues {
        public static final String SCOPE_ACCOUNTS = "accounts";
        public static final String RESPONSE_TYPE_CODE = "code";
    }

    public static final class Transaction {
        public static final String DEBIT = "Debit";
    }

    public enum OAuthGrantTypes {
        @JsonProperty("client_credentials")
        CLIENT_CREDENTIALS,
        @JsonProperty("authorization_code")
        AUTHORIZATION_CODE,
        @JsonProperty("refresh_token")
        REFRESH_TOKEN,
    }

    public static final class HeaderKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";

        // The unique id of the ASPSP to which the request is issued. The unique id will be issued
        // by
        // OB.
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        // The time when the PSU last logged in with the TPP. All dates in the HTTP headers are
        // represented as RFC 7231 Full Dates.
        public static final String X_FAPI_CUSTOMER_LAST_LOGGED_TIME =
                "x-fapi-customer-last-logged-time";
        // The PSU's IP address if the PSU is currently logged in with the TPP. This would be blank
        // in
        // case the PSU is not currently logged in.
        public static final String X_FAPI_CUSTOMER_IP_ADDRESS = "x-fapi-customer-ip-address";
        // An RFC4122 UID used as a correlation id. This id is used to track end-to-end interaction
        // between TPP request and response sent by the API platform.
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    public static final class Permissions {
        public static final String READ_ACCOUNT_BASIC = "ReadAccountsBasic";
        public static final String READ_ACCOUNTS_DETAIL = "ReadAccountsDetail";
        public static final String READ_BALANCES = "ReadBalances";
        public static final String READ_TRANSACTION_BASIC = "ReadTransactionsBasic";
        public static final String READ_TRANSACTIONS_DETAIL = "ReadTransactionsDetail";
        public static final String READ_TRANSCTIONS_CREDTIS = "ReadTransactionsCredits";
        public static final String READ_TRANSACTIONS_DEBITS = "ReadTransactionsDebits";

        public static final ImmutableList<String> ALL_READ_PERMISSIONS =
                ImmutableList.of(
                        READ_ACCOUNT_BASIC,
                        READ_ACCOUNTS_DETAIL,
                        READ_BALANCES,
                        READ_TRANSACTION_BASIC,
                        READ_TRANSACTIONS_DETAIL,
                        READ_TRANSCTIONS_CREDTIS,
                        READ_TRANSACTIONS_DEBITS);
    }

    public static class ErrorMessages {
        public static final String CONSENT_ERROR = "consent_error";
        public static final String INVALID_TOKEN = "invalid_token";
        public static final String MISSING_BALANCE = "No balance available";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find OAuth token.";
        public static final String MISSING_STATE = "OAuth state cannot be null or empty.";
        public static final String MISSING_PERMISSIONS = "Did not receive all permissions";
        public static final String STATUS_CODE_500 = "Status Code: 500";
    }

    public static class ErrorCode {
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
    }

    public static final class StorageKeys {
        public static final String STATE = "state";
        public static final String TOKEN = "token";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }
}
