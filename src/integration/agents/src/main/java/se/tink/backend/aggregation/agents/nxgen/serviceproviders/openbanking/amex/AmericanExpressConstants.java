package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

public class AmericanExpressConstants {

    public static class ErrorMessages {
        public static final String DATE_OUT_OF_RANGE =
                "Request Validation Failed - Requested date range exceeds the supported limit";
    }

    public static class ErrorCodes {
        public static final int DATE_OUT_OF_RANGE = 3027;
    }

    public static class Headers {

        public static final String X_AMEX_API_KEY = "x-amex-api-key";
        public static final String X_AMEX_REQUEST_ID = "x-amex-request-id";
        public static final String AUTHENTICATION = "Authentication";
    }

    public static class QueryParams {
        public static final String QUERY_PARAM_START_DATE = "start_date";
        public static final String QUERY_PARAM_END_DATE = "end_date";
        public static final String QUERY_PARAM_LIMIT = "limit";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE_LIST = "scope_list";
        public static final String STATE = "state";
        public static final String STATUS = "status";
    }

    public static class QueryValues {
        public static final int TRANSACTION_TO_FETCH = 1000;
        public static final String SCOPE_LIST_FOR_AUTHORIZE =
                "MEMBER_ACCT_INFO,FINS_STP_DTLS,FINS_BAL_INFO,FINS_TXN_INFO";
        public static final String SCOPE_LIST_FOR_GET_TOKEN =
                SCOPE_LIST_FOR_AUTHORIZE.replace(',', ' ');
        public static final String PENDING = "pending";
        public static final String POSTED = "posted";
    }

    public static class Urls {
        public static final String RETRIEVE_TOKEN_PATH = "/apiplatform/v2/oauth/token/mac";
        public static final String REFRESH_TOKEN_PATH = "/apiplatform/v1/oauth/token/refresh/mac";
        public static final String REVOKE_TOKEN_PATH = "/apiplatform/v2/oauth/token_revocation/mac";
        public static final String BASE_PATH = "/servicing/v1";
        public static final String ENDPOINT_ACCOUNTS = BASE_PATH + "/member/accounts";
        public static final String ENDPOINT_BALANCES = BASE_PATH + "/financials/balances";
        public static final String ENDPOINT_TRANSACTIONS = BASE_PATH + "/financials/transactions";
    }
}
