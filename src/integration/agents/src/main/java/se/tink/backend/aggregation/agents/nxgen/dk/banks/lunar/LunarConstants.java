package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import java.net.URI;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class LunarConstants {

    public static final String APP_VERSION = "4.26.0";
    public static final String DA_LANGUAGE = "DA";
    private static final String LUNAR_BASE_URL = "https://api.prod.lunarway.com/";

    public static class Uri {
        public static final URI NEM_ID_AUTHENTICATE =
                toLunarUri("authentication/authenticate/nemid");

        public static final URI SIGN_IN = toLunarUri("authentication/signin");

        private static URI toLunarUri(String endpoint) {
            return URI.create(LUNAR_BASE_URL + endpoint);
        }
    }

    public static class Url {
        public static final URL ACCOUNTS_VIEW = toLunarUrl("accounts-view/accounts");

        private static URL toLunarUrl(String endpoint) {
            return URL.of(LUNAR_BASE_URL + endpoint);
        }
    }

    public static class Headers {
        public static final String USER_AGENT = "user-agent";
        public static final String DEVICE_MODEL = "x-devicemodel";
        public static final String REGION = "x-region";
        public static final String OS = "x-os";
        public static final String OS_VERSION = "x-osversion";
        public static final String DEVICE_MANUFACTURER = "x-devicemanufacturer";
        public static final String LANGUAGE = "x-language";
        public static final String ACCEPT_LANGUAGE = "accept-language";
        public static final String REQUEST_ID = "x-requestid";
        public static final String DEVICE_ID = "x-deviceid";
        public static final String ORIGIN = "x-origin";
        public static final String APP_VERSION = "x-appversion";
        public static final String ACCEPT_ENCODING = "accept-encoding";
        public static final String AUTHORIZATION = "authorization";
        public static final String CONTENT_TYPE = "content-type";
    }

    public static class HeaderValues {
        public static final String DEVICE_MODEL =
                DeviceProfileConfiguration.IOS_STABLE.getModelNumber();
        public static final String USER_AGENT_VALUE = "Lunar/99 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String DK_REGION = "DK";
        public static final String DA_LANGUAGE_ACCEPT = "da-dk";
        public static final String I_OS = DeviceProfileConfiguration.IOS_STABLE.getOs();
        public static final String OS_VERSION =
                DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        public static final String DEVICE_MANUFACTURER =
                DeviceProfileConfiguration.IOS_STABLE.getMake();
        public static final String APP_ORIGIN = "App";
        public static final String ENCODING = "gzip, deflate, br";
    }

    public static class Storage {
        public static final String ACCESS_PIN_INPUT_LABEL = "accesspininput";
        public static final String PROCESS_STATE_KEY = "LunarProcessState";
        public static final String PERSISTED_DATA_KEY = "LunarAuthData";
    }

    static class HttpClient {
        static final int MAX_RETRIES = 5;
        static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
