package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class AmericanExpressConstants {

    private AmericanExpressConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static class ErrorMessages {
        private ErrorMessages() {}

        public static final String DATE_OUT_OF_RANGE =
                "Request Validation Failed - Requested date range exceeds the supported limit";

        public static final TypeMapper<Boolean> REVOKED_TOKEN_MAPPER =
                TypeMapper.<Boolean>builder()
                        .put(
                                true,
                                "[ERR_OAS_0001] Access Token expired",
                                "[ERR_OAS_0002] Access Token revoked by user",
                                "[ERR_OAS_0007] Access Token revoked by Change Password Check")
                        .setDefaultTranslationValue(false)
                        .build();
    }

    public static class ErrorCodes {
        private ErrorCodes() {}

        public static final int DATE_OUT_OF_RANGE = 3027;
    }

    public static class Headers {
        private Headers() {}

        public static final String X_AMEX_API_KEY = "x-amex-api-key";
        public static final String X_AMEX_REQUEST_ID = "x-amex-request-id";
        public static final String AUTHENTICATION = "Authentication";
    }

    public static class QueryParams {
        private QueryParams() {}

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
        private QueryValues() {}

        public static final int TRANSACTION_TO_FETCH = 1000;
        public static final String SCOPE_LIST_FOR_AUTHORIZE =
                "MEMBER_ACCT_INFO,FINS_STP_DTLS,FINS_BAL_INFO,FINS_TXN_INFO";
        public static final String SCOPE_LIST_FOR_GET_TOKEN =
                SCOPE_LIST_FOR_AUTHORIZE.replace(',', ' ');
        public static final String PENDING = "pending";
        public static final String POSTED = "posted";
    }

    public static class Urls {
        private Urls() {}

        public static final String RETRIEVE_TOKEN_PATH = "/apiplatform/v2/oauth/token/mac";
        public static final String REFRESH_TOKEN_PATH = "/apiplatform/v1/oauth/token/refresh/mac";
        public static final String REVOKE_TOKEN_PATH = "/apiplatform/v2/oauth/token_revocation/mac";
        public static final String BASE_PATH = "/servicing/v1";
        public static final String ENDPOINT_ACCOUNTS = BASE_PATH + "/member/accounts";
        public static final String ENDPOINT_BALANCES = BASE_PATH + "/financials/balances";
        public static final String ENDPOINT_TRANSACTIONS = BASE_PATH + "/financials/transactions";
    }
}
