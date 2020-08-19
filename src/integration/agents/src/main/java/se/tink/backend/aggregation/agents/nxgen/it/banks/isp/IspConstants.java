package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

public class IspConstants {

    public static String BASE_URL = "https://app-api.intesasanpaolo.com";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss xxxx";

    public static class Endpoints {
        static final String CHECK_PIN = "/ib/content/api/sec/login/checkpin";
        static final String REGISTER_DEVICE = "/ib/content/api/sec/recuperasmartmobilenew";
        static final String REGISTER_DEVICE_2 = "/ib/content/api/sec/recuperasmartmobilestep2new";
        static final String REGISTER_DEVICE_3 = "/ib/content/api/sec/recuperasmartmobilestep3new";
        static final String CHECK_TIME = "/ib/content/api/sec/checktime";
        static final String CONFIRM_DEVICE = "/ib/content/api/sec/confermadevicemobile";
        static final String DISABLE_ALL_BOOKMARKS =
                "/ib/content/api/app/public/menu/disableAllBookMarkMobile";
    }

    public static class Crypto {
        static final String SIGNATURE_CALCULATION_KEY =
                "53bdcf4cf252bf1abbb96106eff46f9bda564a56838ec2a3409ff59bc11ce8a2";
        static final String BODY_ENCRYPTION_KEY = "03993ef3d56532e9795c124a81467c11";
        public static final String CLIENT_ID = "ec63ce28-dde1-4c23-8384-82a198ad74c5";
        public static final String CLIENT_SECRET = "77e76cef-6a1e-4398-9e72-24d8ef833e58";
    }

    public static class HeaderKeys {
        public static final String SIGNATURE = "X-ISP-SIGNATURE";
        public static final String LANG = "LANG";
        static final String CLIENT_VERSION = "ClientVersion";
        public static final String CHANNEL = "CHANNEL";
        public static final String KEY_ID = "X-ISP-KEYID";
        static final String APPLICATION_NAME = "ApplicationName";
        static final String ROOTED = "Rooted";
        static final String OPERATION_SYSTEM = "OperationSystem";
        static final String CALLER = "CALLER";
        public static final String DEVICE_MODEL = "DeviceModel";
        static final String ACCESS_MODE = "AccessMode";
        static final String POWER_SAVING = "X-ISP-Power-Saving";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        static final String CONTENT = "Content-Type";
        public static final String REQUEST_ID = "X-REQUEST-ID";
        static final String USERAGENT = "UserAgent";
        public static final String USER_AGENT = "User-Agent";
        public static final String AUTHORIZATION = "Authorization";
        public static final String DEVICE_ID = "DEVICE-ID";
    }

    public static class HeaderValues {
        public static final String LANG = "IT";
        static final String CLIENT_VERSION = "2.14.0";
        public static final String CHANNEL = "02";
        public static final String KEY_ID = "9934466d-84f1-40be-8244-63601c570dd9";
        static final String APPLICATION_NAME = "IOS";
        static final String ROOTED = "false";
        static final String OPERATION_SYSTEM = "12.4";
        static final String CALLER = "MOBILE|iOS";
        public static final String DEVICE_MODEL = "iPhone9;3";
        static final String ACCESS_MODE = "APP";
        static final String POWER_SAVING = "disabled";
        public static final String ACCEPT_ENCODING = "gzip;q=1.0; compress;q=0.5";
        static final String CONTENT = "application/json;charset=UTF-8";
        static final String USERAGENT =
                "Intesa Sanpaolo Mobile/2.14.0 (com.intesasanpaolo.ibiphone; build:2001040; iOS 12.4.0) Alamofire/2.14.0";
        public static final String USER_AGENT =
                "Intesa Sanpaolo Mobile/2.14.0 (com.intesasanpaolo.ibiphone; build:2001040; iOS 12.4.0) Alamofire/4.9.0";
        public static final String ACCEPT_LANGUAGE = "en-IT;q=1.0";
        public static final String ACCEPT = "*/*";
        static final String AUTHORIZATION_PATTERN = "Bearer %1$s";
    }

    public static class StorageKeys {
        static final String ACCESS_TOKEN = "access_token";
        static final String TRANSACTION_ID = "transaction_id";
        static final String TOTP_MASK = "totp_mask";
        static final String TOTP_DIGITS = "totp_digits";
        static final String DEVICE_ID = "device_id";
    }
}
