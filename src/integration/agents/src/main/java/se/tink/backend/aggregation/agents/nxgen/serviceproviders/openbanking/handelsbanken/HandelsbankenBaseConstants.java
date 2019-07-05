package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class HandelsbankenBaseConstants {
    public static final String INTEGRATION_NAME = "handelsbanken";

    public static class Urls {

        private static final String BASE_URL = "https://api.handelsbanken.com/openbanking";
        private static final String BASE_URL2 = "https://api.handelsbanken.com/bb/gls5";

        private static final String SUFFIX_V1 = "/psd2/v1";
        private static final String SUFFIX_V2 = "/psd2/v2";
        private static final String TOKEN_SUFFIX = "/oauth2/token/1.0";

        public static final URL ACCOUNTS = new URL(BASE_URL + SUFFIX_V2 + "/accounts");
        public static final URL TOKEN = new URL(BASE_URL2 + TOKEN_SUFFIX);
        public static final URL REFRESH_TOKEN = new URL(BASE_URL2 + "/refresh" + TOKEN_SUFFIX);
        public static final URL AUTHORIZATION = new URL(BASE_URL + SUFFIX_V1 + "/consents");
        public static final URL THIRD_PARTIES = new URL(BASE_URL + SUFFIX_V1 + "/third-parties");
        public static final URL SUBSCRIPTIONS = new URL(BASE_URL + SUFFIX_V1 + "/subscriptions");
        public static final URL SESSION =
                new URL(BASE_URL2 + "/decoupled/mbid/initAuthorization/1.0");
        public static final URL ACCOUNT_DETAILS =
                new URL(BASE_URL + SUFFIX_V2 + "/accounts/{" + UrlParams.ACCOUNT_ID + "}");
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(
                        BASE_URL
                                + SUFFIX_V2
                                + "/accounts/{"
                                + UrlParams.ACCOUNT_ID
                                + "}/transactions");
    }

    public static class UrlParams {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class StorageKeys {
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String REFRESH_TOKEN = "refreshToken";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class HeaderKeys {
        public static final String X_IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String TPP_TRANSACTION_ID = "TPP-Transaction-ID";
        public static final String TPP_REQUEST_ID = "TPP-Request-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String AUTHORIZATION = "Authorization";
        public static final String COUNTRY = "country";
        public static final String CONSENT_ID = "consentId";
        public static final String SCOPE = "scope";
        public static final String SESSION_ID = "sessionId";
    }

    public static class BodyKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String BEARER = "Bearer ";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class Market {
        public static final String COUNTRY = "SE";
    }

    public static class BodyValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String AIS_SCOPE = "AIS";
        public static final String ALL_ACCOUNTS = "ALL_ACCOUNTS";
        public static final String PSD2_ADMIN = "PSD2-ADMIN";
        public static final String SUBSCRIPTION_CONSENTS = "consents";

        // "Resource owner Personal Id type, currently only domain value '1' exist. Optional, must
        // exist if personalId is given."
        public static final String PERSONAL_ID_TP = "1";

        public static final String PRODUCT_ACCOUNTS = "accounts";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class AccountBalance {
        public static final String AVAILABLE_BALANCE = "AVAILABLE_AMOUNT";
    }

    public static class Transactions {
        public static final String IS_PENDING = "PENDING";
    }

    public static class ExceptionMessages {
        public static final String BALANCE_NOT_FOUND = "Balance not found.";
        public static final String CONFIG_MISSING = "Handelsbanken configuration missing.";
        public static final String VALUE_DATE_MISSING =
                "Valuedate not found, defaulting to transactiondate.";
        public static final String NOT_PARSE_DATE = "Could not parse date.";
    }

    public static class Errors {
        public static final String MBID_ERROR = "mbid_error";
        public static final String INTENT_EXPIRED = "intent_expired";
        public static final String INVALID_REQUEST = "invalid_request";
        public static final String NOT_SHB_APPROVED = "not_shb_approved";
        public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
        public static final String MBID_MAX_POLLING = "mbid_max_polling";
    }

    public static class Status {
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String USER_CANCEL = "USER_CANCEL";
        public static final String COMPLETE = "COMPLETE";
    }
}
