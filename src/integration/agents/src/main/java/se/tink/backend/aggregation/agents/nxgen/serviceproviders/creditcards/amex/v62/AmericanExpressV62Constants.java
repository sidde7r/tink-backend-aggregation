package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;

public final class AmericanExpressV62Constants {

    private AmericanExpressV62Constants() {
        throw new AssertionError();
    }

    public static final String BASE_API = "https://global.americanexpress.com";
    public static final String NOT_APPLICABLE = "n/a";
    public static final String DASH = "-";
    public static final String NUMBER_REGEX = "[^0-9,.]";

    public enum ConstantValueHeaders implements HeaderEnum {
        CHARSET("Charset", "UTF-8"),
        CLIENT_TYPE("X-AXP-ClientType", "iPhone"),
        DEVICE_MODEL("X-AXP-DeviceModel", "iPhone8.1"),
        DEVICE_OS("X-AXP-DeviceOS", "iOS"),
        OS_VERSION("X-AXP-OSVersion", "13.3.1"),
        MANUFACTURER("x-axp-manufacturer", "Apple"),
        TIMEZONE_OFFSET("X-AXP-TimeZoneOffset", "3600000"),
        TIMEZONE_NAME("x-axp-device-timezone-name", "Europe/Stockholm"),
        ACCEPT_TEXT("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
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

    public static final class Urls {
        public static final String LOG_ON =
                "/mobileone/msl/services/accountservicing/v1/loginsummary";
        public static final String TRANSACTION =
                "/mobileone/msl/services/transactions/v1/getDetails";
        public static final String TIMELINE = "/mobileone/msl/services/timeline/v1/timelineDetail";
        public static final String INITIALIZATION =
                "/mobileone/msl/services/accountservicing/v1/initialization";
        public static final String SANE_ID = "/mobileone/msl/services/app/passthrough";
    }

    public static final class Tags {
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

    public static final class Headers {
        public static final String LOCALE = "X-AXP-Locale";
        public static final String SESSION = "x-axp-amexsession";
        public static final String CUPCAKE = "x-axp-blueboxvalues";
        public static final String INSTALLATION_ID = "X-AXP-AppInstallationId";
        public static final String APP_ID = "X-AXP-AppId";
        public static final String APP_VERSION = "X-AXP-AppVersion";
        public static final String HARDWARE_ID = "X-AXP-HardwareID";
        public static final String PROCESS_ID = "x-axp-process-id";
        public static final String GATEKEEPER = "x-axp-gatekeeper";
        public static final String PUBLIC_GUID = "x-axp-public-guid";
        public static final String REQUEST_SEQUENCE = "x-axp-request-sequence";
        public static final String REQUEST_ID = "x-axp-request-id";
        public static final String GIT_SHA = "x-axp-git-sha";
        public static final String AUTHORIZATION = "authorization";
    }

    public static final class HeadersValue {
        public static final String UNAVAILABLE = "UNAVAILABLE";
    }

    public static final class RequestValue {
        public static final String TIME_ZONE = "GMT+1";
        public static final String TIME_ZONE_OFFSET = "7200000";
    }

    public static final class ReportingCode {
        public static final String LOGON_FAIL_FIRST_ATTEMPT = "LOGON1001";
        public static final String LOGON_FAIL_SECOND_ATTEMPT = "LOGON1002";
        public static final String LOGON_FAIL_ACCOUNT_BLOCKED = "LOGON1003";
        public static final String LOGON_FAIL_CONTENT_ERROR = "LOGON1012";
        public static final String BANKSIDE_TEMPORARY_ERROR = "CDSVC1000";
        public static final String UNSUPPORTED_MARKET = "UNSUPPORTED_MARKET";
    }

    public static final class QueryKeys {
        public static final String PAGE = "page";
        public static final String FACE = "Face";
        public static final String VERSION = "version";
        public static final String CLIENT_TYPE = "clientType";
    }

    public static final class QueryValues {
        public static final String PAGE_VALUE = "eula";
        public static final String FACE_VALUE = "sv_SE";
        public static final String VERSION_VALUE = "23da90ada6b32378654c7c1a88c0e2a808ab3cb3";
        public static final String CLIENT_TYPE_VALUE = "iPhone";
    }

    public static class Fetcher {
        public static final int START_BILLING_INDEX = 0;
        // At least try 5 responses to fetch transactions
        public static final int DEFAULT_MAX_BILLING_INDEX = 5;
    }

    public class Storage {
        public static final String ALL_SUB_ACCOUNTS = "allSubAccounts";
    }

    public static final class StatusCode {
        public static final String INCORRECT = "incorrect";
        public static final String SECOND_ATTEMPT = "secondAttempt";
        public static final String REVOKED = "revoked";
    }

    public static class HttpFilters {
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
        public static final int MAX_NUM_RETRIES = 3;
    }
}
