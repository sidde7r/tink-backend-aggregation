package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

public class IspConstants {

    public static String BASE_URL = "https://app-api.intesasanpaolo.com";
    public static String DEVICE_NAME = "iPhone 7";

    public static class Endpoints {
        public static final String CHECK_PIN = "/ib/content/api/sec/login/checkpin";
        public static final String REGISTER_DEVICE = "/ib/content/api/sec/recuperasmartmobilenew";
        public static final String REGISTER_DEVICE_2 =
                "/ib/content/api/sec/recuperasmartmobilestep2new";
        public static final String REGISTER_DEVICE_3 =
                "/ib/content/api/sec/recuperasmartmobilestep3new";
        public static final String CHECK_TIME = "/ib/content/api/sec/checktime";
        public static final String CONFIRM_DEVICE = "/ib/content/api/sec/confermadevicemobile";
        public static final String RECOVER_DEVICE_LIST =
                "/ib/content/api/app/managedevice/recuperaListaDevice";
        public static final String DISABLE_ALL_BOOKMARKS =
                "/ib/content/api/app/public/menu/disableAllBookMarkMobile";
    }

    public static class Crypto {
        public static final String SIGNATURE_CALCULATION_KEY =
                "53bdcf4cf252bf1abbb96106eff46f9bda564a56838ec2a3409ff59bc11ce8a2";
        public static final String BODY_ENCRYPTION_KEY = "03993ef3d56532e9795c124a81467c11";
        public static final String CLIENT_ID = "b002d501-6dcd-4a0b-b588-b3d729db628a";
        public static final String CLIENT_SECRET = "c97ce265-3782-4c83-802d-48edb65892f2";
    }

    public static class HeaderKeys {
        public static final String SIGNATURE = "X-ISP-SIGNATURE";
        public static final String LANG = "LANG";
        public static final String CLIENT_VERSION = "ClientVersion";
        public static final String CHANNEL = "CHANNEL";
        public static final String KEY_ID = "X-ISP-KEYID";
        public static final String APPLICATION_NAME = "ApplicationName";
        public static final String ROOTED = "Rooted";
        public static final String OPERATION_SYSTEM = "OperationSystem";
        public static final String CALLER = "CALLER";
        public static final String DEVICE_MODEL = "DeviceModel";
        public static final String ACCESS_MODE = "AccessMode";
        public static final String POWER_SAVING = "X-ISP-Power-Saving";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String CONTENT = "Content-Type";
        public static final String REQUEST_ID = "X-REQUEST-ID";
        public static final String USERAGENT = "UserAgent";
        public static final String USER_AGENT = "User-Agent";
        public static final String AUTHORIZATION = "Authorization";
        public static final String DEVICE_ID = "DEVICE-ID";
    }

    public static class HeaderValues {
        public static final String LANG = "IT";
        public static final String CLIENT_VERSION = "2.10.2";
        public static final String CHANNEL = "02";
        public static final String KEY_ID = "9934466d-84f1-40be-8244-63601c570dd9";
        public static final String APPLICATION_NAME = "IOS";
        public static final String ROOTED = "false";
        public static final String OPERATION_SYSTEM = "12.4";
        public static final String CALLER = "MOBILE|iOS";
        public static final String DEVICE_MODEL = "iPhone9;3";
        public static final String ACCESS_MODE = "APP";
        public static final String POWER_SAVING = "disabled";
        public static final String ACCEPT_ENCODING = "gzip;q=1.0; compress;q=0.5";
        public static final String CONTENT = "application/json;charset=UTF-8";
        public static final String USERAGENT =
                "Intesa Sanpaolo Mobile/2.10.2 (com.intesasanpaolo.ibiphone; build:2001040; iOS 12.4.0) Alamofire/2.10.2";
        public static final String USER_AGENT =
                "Intesa Sanpaolo Mobile/2.10.2 (com.intesasanpaolo.ibiphone; build:2001040; iOS 12.4.0) Alamofire/4.9.0";
        public static final String ACCEPT_LANGUAGE = "en-IT;q=1.0";
        public static final String ACCEPT = "*/*";
        public static final String AUTHORIZATION_PATTERN = "Bearer %1$s";
    }
}
