package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public class AmericanExpressV62Constants {

    public static final String BASE_API = "https://global.americanexpress.com";

    public enum ConstantValueHeaders implements HeaderEnum {
        CHARSET("charset", "UTF-8"),
        CLIENT_TYPE("X-AXP-ClientType", "iPhone"),
        APP_VERSION("X-AXP-AppVersion", "6.2.1"),
        DEVICE_MODEL("X-AXP-DeviceModel", "iPhone8,1"),
        DEVICE_OS("X-AXP-DeviceOS", "iOS"),
        HARDWARE_ID("X-AXP-HardwareID", ""),
        OS_VERSION("X-AXP-OSVersion", "10.2"),
        TIMEZONE_OFFSET("X-AXP-TimeZoneOffset", "7200000");

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
    }

    public static final class Tags {
        public static final String SESSION_ID = "sessionId";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String FACE = "face";
        public static final String CARD_LIST = "cardList";
        public static final String IS_PENDING = "pendingTransaction";
        public static final String CUPCAKE = "cupcake";
        public static final String INSTALLATION_ID = "installationId";
    }

    public static final class Headers {
        public static final String LOCALE = "X-AXP-Locale";
        public static final String SESSION = "X-AXP-AmexSession";
        public static final String CUPCAKE = "X-AXP-BlueBoxValues";
        public static final String INSTALLATION_ID = "X-AXP-AppInstallationId";
        public static final String APP_ID = "X-AXP-AppId";
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
        public static final String BANKSIDE_TEMPORARY_ERROR = "CDSVC1000";
        public static final String UNSUPPORTED_MARKET = "UNSUPPORTED_MARKET";
    }

    public static class Fetcher {
        public static final int START_PAGE = 0;
    }
}
