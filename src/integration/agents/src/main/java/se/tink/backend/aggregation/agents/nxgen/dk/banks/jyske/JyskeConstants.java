package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class JyskeConstants {

    public static final String INTEGRATION_NAME = "jyskebank-dk";
    public static final Charset CHARSET = Charsets.UTF_8;

    public static final class Url {
        private static final String BANKDATA_BASE_URL = "https://mobil.bankdata.dk/mobilbank";
        private static final String SERVICE_BASE_URL =
                "https://mobile-services.jyskebank.dk/mobilebank.services/rest";

        public static final URL NEMID_INIT = toBankDataUrl("/nemid/init");
        public static final URL NEMID_GET_CHALLANGE = toBankDataUrl("/nemid/get_challange");
        public static final URL NEMID_ENROLL = toBankDataUrl("/nemid/inroll");
        public static final URL NEMID_LOGIN = toBankDataUrl("/nemid/login_with_installid_prop");
        public static final URL GENERATE_CODE = toBankDataUrl("/nemid/generatecode");
        public static final URL CHANGE_TO_KEYCARD = toBankDataUrl("/nemid/changetokeycard");

        public static final URL GET_ACCOUNTS_WITH_EXTERNALS = toBankDataUrl("/accounts");

        public static final URL LOGOUT = toBankDataUrl("/invalidate");
        public static final URL TRANSPORT_KEY = toMobileServiceUrl("/V1-0/transportkey");
        public static final URL MOBILE_SERVICE_LOGIN = toMobileServiceUrl("/V1-0/login");

        private static URL toBankDataUrl(String endpoint) {
            return new URL(BANKDATA_BASE_URL + endpoint);
        }

        private static URL toMobileServiceUrl(String endpoint) {
            return new URL(SERVICE_BASE_URL + endpoint);
        }
    }

    public static final class Header {
        public static final String APP_ID_KEY = "x-app-id";
        public static final String APP_ID_VALUE = "ios_phone_jyskemobilbank";
        public static final String APPID_KEY = "x-appid";
        public static final String APPID_VALUE = APP_ID_VALUE;
        public static final String VERSION_KEY = "x-version";
        public static final String VERSION_VALUE = "3.20.5";
        public static final String BANKNO_KEY = "x-bankNo";
        public static final String BANKNO_VALUE = "51";
        public static final String OS_KEY = "x-os";
        public static final String OS_VALUE = "ios";

        public static final String BUILDNO_KEY = "x-buildNo";
        public static final String BUILDNO_VALUE = "1364";

        public static final String PERSONALID_KEY = "x-personalId";
    }

    public static final class Crypto {
        public static final String RSA_LABEL = "jbprodver001";
        public static final String CERT_TYPE = "X.509";
    }

    public static final class Storage {
        public static final String TOKEN = "token";
        public static final String INSTALL_ID = "installId";
        public static final String USER_ID = "userId";
        public static final String PIN_CODE = "pin";
        public static final String NEMID_CHALLENGE_ENTITY = "nemidChallengeEntity";
        public static final String NEMID_LOGIN_ENTITY = "nemidLoginResponse";
        public static final String KEYCARD_CHALLENGE_ENTITY = "keycardChallengeEntity";
    }

    public static final class ErrorCode {
        public static final int INVALID_CREDENTIAL = 112;
        public static final int NOT_SIGNED_UP_FOR_MOBILE_BANK = 109;
        public static final int INROLL_BAD_REQUEST = 1;
    }

    public static final class ErrorMessages {
        public static final String BANK_UNAVAILABLE_DURING_MIDNIGHT =
                "mobilbanken er lukket hverdage og ";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
