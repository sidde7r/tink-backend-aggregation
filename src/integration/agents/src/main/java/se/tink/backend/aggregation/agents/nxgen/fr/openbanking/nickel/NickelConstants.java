package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NickelConstants {

    public static final String EMPTY_STRING = "";
    public static final String SIGNATURE_FORMAT =
            "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
    public static final List<String> SIGNABLE_HEADERS =
            ImmutableList.of(
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
    public static final String STATUS_PENDING = "PENDING";
    public static final String DEVICE_TYPE = "CHROME";
    public static final String APP_TYPE = "WEB";
    public static final String APP_VERSION = "1.00.0";
    public static final String DEVICE_NAME = "Tink";
    public static final String NICKEL_IBAN_FORMAT = "FR761659800001%s%02d";
    public static final Long NICKEL_MAGIC_NUMBER = 1477237L;
    public static final Long SESION_EXPIRED_AFTER_DAYS = 90L;

    public static final int BASE_URL_LENGTH = URLs.BASE_URL.length();
    protected static final Map<String, Object> NICKEL_HEADERS = buildHeaders();

    private static Map<String, Object> buildHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.AUTHORITY, URLs.NICKEL_API);
        headers.put(HeaderKeys.SCHEME, HeaderValues.HTTPS);
        headers.put(HeaderKeys.SEC_FETCH_SITE, HeaderValues.SAME_SITE);
        headers.put(HeaderKeys.SEC_FETCH_MODE, HeaderValues.CORS);
        headers.put(HeaderKeys.SEC_FETCH_DEST, HeaderValues.EMPTY);
        headers.put(HeaderKeys.PRAGMA, HeaderValues.NO_CACHE);
        headers.put(HeaderKeys.CACHE_CONTROL, HeaderValues.NO_CACHE);
        headers.put(HeaderKeys.PERSONAL_SPACE, HeaderValues.PERSONAL_SPACE);
        headers.put(HeaderKeys.ACCEPT_ENCODING, HeaderValues.GZIP_DEFLATE_BR);
        headers.put(HeaderKeys.ACCEPT_LANGUAGE, HeaderValues.LANGUAGES);
        headers.put(HeaderKeys.ORIGIN, URLs.BASE_URL);
        headers.put(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT);
        headers.put(HeaderKeys.CONNECTION, HeaderValues.KEEP_ALIVE);
        headers.put(HeaderKeys.REFERER, URLs.BASE_URL + "/");
        return headers;
    }

    public static class URLs {
        public static final String NICKEL_API = "api.nickel.eu";
        public static final String BASE_URL = "https://" + NICKEL_API;
        public static final String AUTHENTICATION_URL =
                BASE_URL + "/customer-authentication-api-v2/v1";
        public static final String MINIMUM_VIABLE_AUTHENTICATIONS_URL =
                AUTHENTICATION_URL + "/minimum-viable-authentications";
        public static final String SMS_CODE_REQUEST = AUTHENTICATION_URL + "/sms-code-requests";
        public static final String SMS_CODE_VERIFICATIONS =
                AUTHENTICATION_URL + "/sms-code-verifications";
        public static final String CUSTOMER_BANKING_URL = BASE_URL + "/customer-banking-api";
        public static final String ACCOUNTS_PATH = CUSTOMER_BANKING_URL + "/accounts";
        public static final String ACCOUNTS_OVERVIEW_PATH =
                CUSTOMER_BANKING_URL + "/accounts/%s/overview";
        public static final String USER_OPERATIONS_URL =
                CUSTOMER_BANKING_URL + "/accounts/%s/operations";
        public static final String PRIMARY_ACCOUNT_DETAILS_URL =
                CUSTOMER_BANKING_URL + "/customers/%s/accounts/primary/details";
        public static final String PERSONAL_SPACE_URL = BASE_URL + "/personal-space-api";
        public static final String USER_PATH = PERSONAL_SPACE_URL + "/api/v2/users";
        public static final String USER_DATA_URL = USER_PATH + "/%s";
        public static final String USER_PERSONAL_AUTH =
                PERSONAL_SPACE_URL + "/api/v3/authentications";
        public static final String SEND_SMS_CODE =
                PERSONAL_SPACE_URL + "/api/v2/challenges/sms/codes";
        public static final String VALIDATE_SMS_CODE =
                PERSONAL_SPACE_URL + "/api/v2/challenges/sms/codes";

        private URLs() {}
    }

    public static class ErrorMessages {
        public static final String MISSING_TKN = "Cannot find token";
        public static final String BODY_SERIALIZATION_ERROR =
                "Unable to perform signing, cannot serialize request body";
        public static final String SIGNING_FILTER_CREATION_ERROR =
                "Could not create nickel signing filter due to certificate parsing errors";
        public static final String INVALID_QSEALC_CERTIFICATE = "Invalid QSealc certificate";

        private ErrorMessages() {}
    }

    public static class QueryKeys {
        public static final String CHALLENGE_TKN = "challenge-token";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String DATE_FROM = "start";
        public static final String DATE_TO = "end";
        public static final String RESPONSE_MODE = "response_mode";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String PAGE = "page";
        public static final String CUSTOMER_ID = "customer_id";
        public static final String LABEL = "label";

        private QueryKeys() {}
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SCRT = "clientSecret";
        public static final String MFA_TKN = "mfaToken";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String PSU_ID = "PSU_ID";
        public static final String ACCESS_TKN = "accessToken";
        public static final String PERSONAL_ACCESS_TKN = "personalAccessToken";
        public static final String ID_TKN = "personalIdToken";
        public static final String SMS_TKN = "SMSToken";
        public static final String PERSONAL_API_CHECK = "personalApiCheck";
        public static final String CUSTOMER_ID = "customerId";
        public static final String DEVICE_ID = "deviceId";

        private StorageKeys() {}
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "authorization";
        public static final String AUTHORITY = ":authority";
        public static final String AUTH_CHALLENGE_TKN = "auth-challenge-token";
        public static final String CONNECTION = "Connection";
        public static final String SIGNATURE = "signature";
        public static final String PSU_DEVICE_ID = "PSU-Device-ID";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_IP_PORT = "PSU-IP-Port";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
        public static final String USER_AGENT = "user-agent";
        public static final String REQUEST_ID = "Request-ID";
        public static final String TPP_CLIENT_ID = "TPP-Client-ID";
        public static final String TPP_ETSI_AUTHORIZATION_NUMBER = "tpp-etsi-authorization-number";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String TPP_SIGNATURE_TIMESTAMP = "tpp-signature-timestamp";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DATE = "Date";
        public static final String DIGEST = "digest";
        public static final String HOST = "host";
        public static final String METHOD = ":method";
        public static final String REQUEST_TARGET = "(request-target)";
        public static final String SESSION_ID = "session-id";
        public static final String CONTENT_TYPE = "content-type";
        public static final String ORIGIN = "origin";
        public static final String REFERER = "referer";
        public static final String ACCEPT_ENCODING = "accept-encoding";
        public static final String ACCEPT_LANGUAGE = "accept-language";
        public static final String CONTENT_LENGTH = "content-length";
        public static final String NUDETECT_PAYLOAD = "nudetect_payload";
        public static final String PATH = ":path";
        public static final String PERSONAL_SPACE = "personal-space";
        public static final String SCHEME = ":scheme";
        public static final String SEC_FETCH_SITE = "sec-fetch-site";
        public static final String SEC_FETCH_MODE = "sec-fetch-mode";
        public static final String SEC_FETCH_DEST = "sec-fetch-dest";
        public static final String PRAGMA = "pragma";
        public static final String CACHE_CONTROL = "cache-control";

        private HeaderKeys() {}
    }

    public static class HeaderValues {
        public static final String BASIC = "Basic ";
        public static final String JSON_CONTENT = "application/json";
        public static final String ACCEPT = JSON_CONTENT + ", text/plain, */*";
        public static final String USER_AGENT = "Tink";
        public static final String PSU_USER_AGENT = USER_AGENT;
        public static final String GZIP_DEFLATE_BR = "gzip, deflate, br";
        public static final String LANGUAGES = "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7,fr;q=0.6";
        public static final String BEARER_FORMAT = "Bearer %s";
        public static final String PERSONAL_SPACE = "web/" + APP_VERSION;
        public static final String KEEP_ALIVE = "keep-alive";
        public static final String HTTPS = "https";
        public static final String SAME_SITE = "same-site";
        public static final String CORS = "coors";
        public static final String EMPTY = "empty";
        public static final String NO_CACHE = "no-cache";

        private HeaderValues() {}
    }
}
