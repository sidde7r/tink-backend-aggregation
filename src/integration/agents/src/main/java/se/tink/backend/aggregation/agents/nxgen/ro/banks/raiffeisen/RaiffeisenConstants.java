package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class RaiffeisenConstants {

    //TODO: Move to development.yaml
    // These are only for sandbox!
    public static final String CLIENT_ID_VALUE = "52c20546-32c0-42b1-afa2-18770029ec80";
    public static final String CLIENT_SECRET_VALUE = "P5cM0oP1sM5iV8uP3fF6dV3bU8uI0fD5kG2dO1sB1eF2pJ4pS4";
    public static final String REDIRECT_URL_VALUE = "https://127.0.0.1:7357/api/v1/thirdparty/callback";
    // for localhost: https://127.0.0.1:7357/api/v1/thirdparty/callback
    // for staging: https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback

    public static final class URL {
        public static final String BASE_AUTH = "https://api-test.raiffeisen.ro/psd2-sandbox-oauth2-api";
        public static final String BASE_API = "https://api-test.raiffeisen.ro/";
        public static final String OAUTH = "/oauth2/authorize";
        public static final String TOKEN = "/oauth2/token";
        public static final String ACCOUNTS = "/v1/banks/rbro/sandbox/accounts";
        public static final String TRANSACTIONS = "/v1/banks/rbro/sandbox/accounts/%s/transactions";
    }

    public static final class QUERY {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URL = "redirect_uri";
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "pageSize";

        public static final String SCOPE_VALUE = "AISP";
        public static final String RESPONSE_TYPE_CODE = "code";
        public static final String WITH_BALANCE_TRUE = "true";
        public static final String BOOKING_STATUS_BOTH = "both";
    }

    public static final class HEADER {
        public static final String ACCEPT_TEXT_HTML = "text/html";
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static final class BODY {
        public static final String GRANT_TYPE_CODE = "authorization_code";
        public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    }

    public static final class STORAGE {
        public static final String TOKEN = "TOKEN";
        public static final String BALANCE_URL = "BALANCE_URL";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static final class DATE {
        public static final String FORMAT = "yyyy-MM-dd";
        public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(FORMAT);
    }

    public static final class FORM {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static final class REGEX {
        public static final Pattern PAGE = Pattern.compile("page=(.*)");
        public static final Pattern PATTERN_STRUCTURED_INFO = Pattern.compile(".*\\\"MerchantName\\\" ?: ?\\\"(.*)\\\".*");
    }

}
