package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

public class AmericanExpressConstants {

    public static final String BASE_API = "https://global.americanexpress.com";

    public static final class Urls {
        public static final String LOG_IN =
                "/myca/intl/moblclient/emea/services/accountservicing/v1/loginSummary?Face=";
        public static final String TRANSACTION =
                "/myca/intl/moblclient/emea/svc/v1/transaction.do?Face=";
        public static final String TIMELINE =
                "/myca/intl/moblclient/emea/services/timeline/v1/timelineDetail?Face=";
        public static final String EXTEND_SESSION =
                "/myca/intl/moblclient/emea/services/accountservicing/v1/extendSession?Face=";
        public static final String LOG_OUT =
                "/myca/intl/moblclient/emea/services/accountservicing/v1/logoff?Face=";
    }

    public static final class Tags {
        public static final String SESSION_ID = "sessionId";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String FACE = "face";
        public static final String CARD_LIST = "cardList";
        public static final String IS_PENDING = "pendingTransaction";
        public static final String CUPCAKE = "cupcake";
    }

    public static final class Headers {
        public static final String APP_ID = "appID";
        public static final String USER_AGENT = "User-Agent";
        public static final String CHARSET = "charset";
        public static final String CLIENT_TYPE = "clientType";
        public static final String CLIENT_VERSION = "clientVersion";
        public static final String DEVICE_ID = "deviceId";
        public static final String DEVICE_MODEL = "deviceModel";
        public static final String OS_BUILD = "osBuild";
        public static final String HARDWARE_ID = "hardwareID";
        public static final String OS_VERSION = "oSversion";
        public static final String FACE = "Face";
        public static final String SESSION = "AmexSession";
        public static final String CUPCAKE = "cupcake";
    }

    public static final class HeaderValues {
        public static final String CHARSET = "UTF-8";
        public static final String CLIENT_TYPE = "iPhone";
        public static final String CLIENT_VERSION = "4.5.0";
        public static final String DEVICE_MODEL = "iPhone8,1";
        public static final String OS_BUILD = "iOS 10.2";
        public static final String OS_VERSION = "10.2";
    }

    public static final class RequestValue {
        public static final String TRUE = "true";
        public static final String TIME_ZONE = "GMT+1";
        public static final String TIME_ZONE_OFFSET = "3600000";
    }

    public static final class StatusCode {
        public static final String INCORRECT = "incorrect";
        public static final String SECOND_ATTEMPT = "secondAttempt";
        public static final String REVOKED = "revoked";
    }
}
