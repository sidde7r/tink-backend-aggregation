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
        AUTHORITY("authority", "global.americanexpress.com"),
        CHARSET("charset", "UTF-8"),
        CLIENT_TYPE("x-axp-clienttype", "iPhone"),
        APP_VERSION("x-axp-appversion", "6.24.0"),
        DEVICE_MODEL("x-axp-devicemodel", "iPhone8,1"),
        DEVICE_OS("x-axp-deviceos", "iOS"),
        OS_VERSION("x-axp-osversion", "12.4.3"),
        MANUFACTURER("x-axp-manufacturer", "Apple"),
        TIMEZONE_OFFSET("x-axp-timezoneoffset", "3600000"),
        TIMEZONE_NAME("x-axp-device-timezone-name", "Europe/Stockholm"),
        ACCEPT_ENCODING("accept-encoding", "br, gzip, deflate"),
        ACCEPT_LANGUAGE("accept-language", "en-us");

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
        public static final String EXTEND_SESSION =
                "/mobileone/msl/services/accountservicing/v1/extendSession";
        public static final String LOG_OUT = "/mobileone/msl/services/accountservicing/v1/logoff";
        public static final String INITIALIZATION =
                "/mobileone/msl/services/accountservicing/v1/initialization";
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
    }

    public static final class Headers {
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
        public static final String REQUEST_ID = "x-axp-request-id";
        public static final String GIT_SHA = "x-axp-git-sha";
        public static final String AUTHORIZATION = "authorization";
    }

    public static final class HeadersValue {
        public static final String UNAVAILABLE = "UNAVAILABLE";
    }

    public static final class RequestValue {
        public static final String TRUE = "true";
        public static final String TIME_ZONE = "GMT+1";
        public static final String TIME_ZONE_OFFSET = "3600000";
    }

    public static final class ReportingCode {
        public static final String LOGON_FAIL_FIRST_ATTEMPT = "LOGON1001";
        public static final String LOGON_FAIL_SECOND_ATTEMPT = "LOGON1002";
        public static final String LOGON_FAIL_ACCOUNT_BLOCKED = "LOGON1003";
        public static final String LOGON_FAIL_CONTENT_ERROR = "LOGON1012";
        public static final String BANKSIDE_TEMPORARY_ERROR = "CDSVC1000";
        public static final String UNSUPPORTED_MARKET = "UNSUPPORTED_MARKET";
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
