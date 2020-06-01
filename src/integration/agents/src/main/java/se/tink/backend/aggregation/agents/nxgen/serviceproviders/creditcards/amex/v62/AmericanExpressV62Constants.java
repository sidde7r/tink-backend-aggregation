package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.Locale;
import java.util.TimeZone;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressV62Constants {

    private AmericanExpressV62Constants() {
        throw new AssertionError();
    }

    public static final String BASE_API = "https://global.americanexpress.com";
    public static final String KEY_EXCHANGE_URL =
            "https://apigateway.americanexpress.com/payments/digital/v1/trust/device/keys";
    public static final String NOT_APPLICABLE = "n/a";
    public static final String DASH = "-";
    public static final String NUMBER_REGEX = "[^0-9,.]";

    public enum ConstantValueHeaders implements HeaderEnum {
        AUTHORITY(":authority", "global.americanexpress.com"),
        CHARSET("charset", "UTF-8"),
        CLIENT_TYPE("x-axp-clienttype", "iPhone"),
        DEVICE_MODEL("x-axp-devicemodel", "iPhone9,4"),
        DEVICE_OS("x-axp-deviceos", "iOS"),
        OS_VERSION("x-axp-osversion", "13.3.1"),
        MANUFACTURER("x-axp-manufacturer", "Apple"),
        TIMEZONE_OFFSET("x-axp-timezoneoffset", "7200000"),
        TIMEZONE_NAME("x-axp-device-timezone-name", "Europe/Stockholm"),
        ACCEPT_TEXT("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
        ACCEPT_JSON("accept", "application/json"),
        CONTENT_TYPE_JSON("content-type", "application/json"),
        ACCEPT_ENCODING("accept-encoding", "gzip, deflate, br"),
        ACCEPT_LANGUAGE("accept-language", "en-us"),
        USER_AGENT(
                "user-agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148");

        private final String headerName;
        private final String headerValue;

        ConstantValueHeaders(String headerName, String headerValue) {

            this.headerName = headerName;
            this.headerValue = headerValue;
        }

        @Override
        public String getKey() {
            return headerName;
        }

        @Override
        public String getValue() {
            return headerValue;
        }
    }

    public static class Urls {
        public static final String LOG_ON =
                "/mobileone/msl/services/accountservicing/v1/loginsummary";
        public static final String TRANSACTION =
                "/mobileone/msl/services/transactions/v1/getDetails";
        public static final String TIMELINE = "/mobileone/msl/services/timeline/v1/timelineDetail";
        public static final String INITIALIZATION =
                "/mobileone/msl/services/accountservicing/v1/initialization";
        public static final String SANE_ID = "/mobileone/msl/services/app/passthrough";
    }

    public static class Tags {
        public static final String SESSION_ID = "sessionId";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String CARD_LIST = "cardList";
        public static final String IS_PENDING = "pendingTransaction";
        public static final String CUPCAKE = "cupcake";
        public static final String INSTALLATION_ID = "installationId";
        public static final String USER_DATA = "userData";
        public static final String GATEKEEPER = "gatekeeperCookie";
        public static final String PROCESS_ID = "processId";
        public static final String MASKED_USER_ID = "maskedUserId";
        public static final String REMEMBER_ME_TOKEN = "rememberMeToken";
        public static final String PUBLIC_GUID = "publicGuid";
        public static final String AUTHORIZATION = "authorization";
        public static final String INIT_VERSION = "initVersion";
    }

    public static class Headers {
        public static final String LOCALE = "x-axp-locale";
        public static final String SESSION = "x-axp-amexsession";
        public static final String CUPCAKE = "x-axp-blueboxvalues";
        public static final String INSTALLATION_ID = "x-axp-appinstallationid";
        public static final String APP_ID = "x-axp-appid";
        public static final String APP_VERSION = "x-axp-appversion";
        public static final String HARDWARE_ID = "x-axp-hardwareid";
        public static final String PROCESS_ID = "x-axp-process-id";
        public static final String GATEKEEPER = "x-axp-gatekeeper";
        public static final String PUBLIC_GUID = "x-axp-public-guid";
        public static final String REQUEST_SEQUENCE = "x-axp-request-sequence";
        public static final String REQUEST_ID = "x-axp-request-id";
        public static final String GIT_SHA = "x-axp-git-sha";
        public static final String AUTHORIZATION = "authorization";
        public static final String DEVICE_TIME = "x-axp-device-time";
        public static final String USER_AGENT = "user-agent";
        public static final String TRACKING_ID = "tracking_id";
        public static final String APP_BLOCK = "check_app_block";
        public static final String CLIENT_ID = "client_id";
    }

    public static class HeadersValue {
        public static final String UNAVAILABLE = "UNAVAILABLE";
        public static final String COMMIT_HASH_V29 = "f44e82ae68b";
        public static final String COMMIT_HASH_V30 = "bb326a4ecd7";
        public static final String START_DATE_V29 = "19/12/31 00:00:00";
        public static final String START_DATE_V30 = "20/01/01 00:00:00";
        public static final String DEVICE_TYPE = "Phone";
        public static final String CLIENT_VERSION = "1.0";
        public static final String APP_BLOCK =
                "com.americanexpress.mobilepayments.ios.paymentframework:1.0";
        public static final String CLIENT_ID = "amexpayiospf";
    }

    public static class RequestValue {
        public static final String TRUE = "true";
        public static final String TIME_ZONE = "GMT+1";
        public static final String TIME_ZONE_OFFSET = "7200000";
    }

    public static class ReportingCode {
        public static final String LOGON_FAIL_FIRST_ATTEMPT = "LOGON1001";
        public static final String LOGON_FAIL_SECOND_ATTEMPT = "LOGON1002";
        public static final String LOGON_FAIL_ACCOUNT_BLOCKED = "LOGON1003";
        public static final String LOGON_FAIL_CONTENT_ERROR = "LOGON1012";
        public static final String BANKSIDE_TEMPORARY_ERROR = "CDSVC1000";
        public static final String UNSUPPORTED_MARKET = "UNSUPPORTED_MARKET";
    }

    public static class QueryKeys {
        public static final String PAGE = "page";
        public static final String FACE = "Face";
        public static final String VERSION = "version";
        public static final String CLIENT_TYPE = "clientType";
    }

    public static class QueryValues {
        public static final String PAGE_VALUE = "eula";
        public static final String FACE_VALUE = "sv_SE";
        public static final String VERSION_VALUE = "23da90ada6b32378654c7c1a88c0e2a808ab3cb3";
        public static final String CLIENT_TYPE_VALUE = "iPhone";
    }

    public static class CryptoKeys {
        public static final String RSA_PUBLIC_KEY =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuRK3RZ0A/GvvLFiGSCBhDKa4AVjwVIHE/rSAAaFLPNWfdEmS5FmiidDQpLXOHkHD9aAMzPKWRd5x1RmOKmKE3H9nmFaZQnGf5s+OOqzypnuckmr88mMRo+sa3HtyamfE7YnLZgUfaG35/nxc7cnMVRuZn9/SApKaGwRnwRThR2OW4c4YB2xXjCAsM5x4wtfXpXfExQFd8hWAWPpvxWIWhh4Tos2FSXWGYYm5FDk3oVOJebTV5sVBSH5avoMmz30lQ4KgrRBU0uLWBpwgivh+zXEbl6JMp5M5OYsEqjb3gYnBjDfyHVgFVBuMg+Dp93xI7LmmvsptxlyG2RoksrSlfQIDAQAB";
        public static final String ZERO_IV_BASE64 = "AAAAAAAAAAAAAAAAAAAAAA==";
        public static final String HMAC_SALT_BASE64 = "AAAAAAAAA6A=";
    }

    public static class Fetcher {
        public static final int START_BILLING_INDEX = 0;
        // At least try 5 responses to fetch transactions
        public static final int DEFAULT_MAX_BILLING_INDEX = 5;
    }

    public static class Storage {
        public static final String ALL_SUB_ACCOUNTS = "allSubAccounts";
        public static final String RANDOM_HEX = "randomHex";
        public static final String ENCRYPTED_KEYS = "encryptedKeys";
    }

    public static class StatusCode {
        public static final String INCORRECT = "incorrect";
        public static final String SECOND_ATTEMPT = "secondAttempt";
        public static final String REVOKED = "revoked";
    }

    public static class HttpFilters {
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
        public static final int MAX_NUM_RETRIES = 3;
    }

    public static class PATTERN {
        public static final ThreadSafeDateFormat DATE_FORMATTER =
                new ThreadSafeDateFormat.ThreadSafeDateFormatBuilder(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                new Locale("sv", "SE"),
                                TimeZone.getTimeZone("UTC"))
                        .build();
    }
}
