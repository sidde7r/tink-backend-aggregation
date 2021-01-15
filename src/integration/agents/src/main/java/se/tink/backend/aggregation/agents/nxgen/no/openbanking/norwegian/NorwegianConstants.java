package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian;

import java.util.Arrays;
import java.util.List;

public class NorwegianConstants {

    public static final String SIGNATURE_FORMAT =
            "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
    public static final List<String> SIGNABLE_HEADERS =
            Arrays.asList(
                    "(request-target)",
                    HeaderKeys.DATE,
                    HeaderKeys.DIGEST,
                    HeaderKeys.REQUEST_ID,
                    HeaderKeys.HOST,
                    HeaderKeys.PSU_DEVICE_ID,
                    HeaderKeys.PSU_ID,
                    HeaderKeys.PSU_IP_ADDRESS,
                    HeaderKeys.PSU_IP_PORT,
                    HeaderKeys.PSU_USER_AGENT,
                    HeaderKeys.TPP_CLIENT_ID,
                    HeaderKeys.TPP_REDIRECT_URI);

    public static class URLs {
        public static final String BASE_URL = "https://api.banknorwegian.com/openbanking";
        public static final String TOKEN_URL = "https://identity.banknorwegian.no/connect/token";
        public static final String ACCOUNTS_PATH = "/accounts";
        public static final String BALANCES_PATH = "/accounts/{accountResourceId}/balances";
        public static final String TRANSACTIONS_PATH = "/accounts/{accountResourceId}/transactions";
        public static final String CONSENT_PATH = "/consents";
        public static final String CONSENT_DETAILS_PATH = "/consents/{consentId}";
    }

    public static class ErrorMessages {
        public static final String MISSING_TOKEN = "Cannot find token";
    }

    public class IdTags {
        public static final String ACCOUNT_RESOURCE_ID = "accountResourceId";
        public static final String CONSENT_ID = "consentId";
    }

    public class ResponseValues {
        public static final String BALANCE_TYPE_EXPECTED = "Expected";
        public static final String BALANCE_TYPE_CLOSING = "ClosingBooked";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String BEARER = "Bearer";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String RESPONSE_MODE = "response_mode";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String PAGE = "page";
    }

    public static class QueryValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String QUERY = "query";
        public static final String BOOKING_STATUS_BOTH = "both";
    }

    public static class StorageKeys {
        public static final String TOKEN = "OAUTH_TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String PSU_ID = "PSU_ID";
        public static final String CONSENT_CREATION_DATE = "CONSENT_CREATION_DATE";
        public static final String FETCHED_90_DAYS_OF_TRANSACTIONS =
                "FETCHED_90_DAYS_OF_TRANSACTIONS";
    }

    public class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String SIGNATURE = "signature";
        public static final String PSU_DEVICE_ID = "PSU-Device-ID";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_IP_PORT = "PSU-IP-Port";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
        public static final String REGION_ID = "Region-ID";
        public static final String REQUEST_ID = "Request-ID";
        public static final String TPP_CLIENT_ID = "TPP-Client-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DATE = "Date";
        public static final String DIGEST = "Digest";
        public static final String HOST = "Host";
    }

    public class HeaderValues {
        public static final String BASIC = "Basic ";
        public static final String PSU_DEVICE_ID = "Tink";
        public static final String PSU_USER_AGENT = "Tink";
        public static final String REGION_ID = "NO";
        public static final String PSU_PORT = "0000";
    }
}
