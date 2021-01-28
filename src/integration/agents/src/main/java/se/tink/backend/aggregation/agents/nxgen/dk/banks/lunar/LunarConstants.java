package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import java.net.URI;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class LunarConstants {

    static final String APP_VERSION = "4.26.0";
    static final String DA_LANGUAGE = "DA";
    private static final String LUNAR_BASE_URL = "https://api.prod.lunarway.com/";

    static class Uri {
        static final URI NEM_ID_AUTHENTICATE = toLunarUri("authentication/authenticate/nemid");

        static final URI SIGN_IN = toLunarUri("authentication/signin");

        private static URI toLunarUri(String endpoint) {
            return URI.create(LUNAR_BASE_URL + endpoint);
        }
    }

    static class Url {
        static final URL ACCOUNTS_VIEW = toLunarUrl("accounts-view/accounts");

        private static URL toLunarUrl(String endpoint) {
            return URL.of(LUNAR_BASE_URL + endpoint);
        }
    }

    static class Headers {
        static final String USER_AGENT = "user-agent";
        static final String DEVICE_MODEL = "x-devicemodel";
        static final String REGION = "x-region";
        static final String OS = "x-os";
        static final String OS_VERSION = "x-osversion";
        static final String DEVICE_MANUFACTURER = "x-devicemanufacturer";
        static final String LANGUAGE = "x-language";
        static final String ACCEPT_LANGUAGE = "accept-language";
        static final String REQUEST_ID = "x-requestid";
        static final String DEVICE_ID = "x-deviceid";
        static final String ORIGIN = "x-origin";
        static final String APP_VERSION = "x-appversion";
        static final String ACCEPT = "accept";
        static final String ACCEPT_ENCODING = "accept-encoding";
        static final String AUTHORIZATION = "authorization";
        static final String CONTENT_TYPE = "content-type";
    }

    static class HeaderValues {
        static final String DEVICE_MODEL = DeviceProfileConfiguration.IOS_STABLE.getModelNumber();
        static final String USER_AGENT_VALUE = "Lunar/99 CFNetwork/1121.2.2 Darwin/19.3.0";
        static final String DK_REGION = "DK";
        static final String DA_LANGUAGE_ACCEPT = "da-dk";
        static final String I_OS = DeviceProfileConfiguration.IOS_STABLE.getOs();
        static final String OS_VERSION = DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        static final String DEVICE_MANUFACTURER = DeviceProfileConfiguration.IOS_STABLE.getMake();
        static final String APP_ORIGIN = "App";
        static final String ACCEPT_ALL = "*/*";
        static final String ENCODING = "gzip, deflate, br";
    }

    public static class Storage {
        public static final String ACCESS_PIN_INPUT_LABEL = "accesspininput";
    }

    static class HttpClient {
        static final int MAX_RETRIES = 5;
        static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
