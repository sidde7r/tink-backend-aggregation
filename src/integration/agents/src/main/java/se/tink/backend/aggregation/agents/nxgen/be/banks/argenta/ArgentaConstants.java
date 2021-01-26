package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ArgentaConstants {

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CHECKING")
                    .put(TransactionalAccountType.SAVINGS, "SAVINGS", "ESAVINGS")
                    .build();

    public static final class Url {
        private static final String BASE = "https://mobile-api.argenta.be/";
        static final URL KEEP_ALIVE_URL = new URL(BASE + "authentication/keep-alive");
        public static final URL CONFIG = new URL(BASE + "config");
        public static final URL AUTH_START = new URL(BASE + "authentication/start-auth");
        static final URL AUTH_VALIDATE = new URL(BASE + "authentication/validate-auth");
        static final URL ACCOUNTS = new URL(BASE + "accounts");
        static final String TRANSACTIONS = "/transactions";
    }

    public static final ImmutableMap<String, String> HEADERS =
            ImmutableMap.<String, String>builder()
                    .put("App-Version", Application.APPLICATION_VERSION)
                    .put("User-Agent", Application.USER_AGENT)
                    .put("Accept-Language", "nl")
                    .build();

    public static class HEADER {
        public static final String DEVICE_ID = "Device-Id";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class PARAMETERS {
        public static final String PAGE = "page";
    }

    public static class Storage {
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String HOME_OFFICE = "HOME_OFFICE";
        public static final String UAK = "UAK";
        public static final String IS_NEW_CREDENTIAL = "IS_NEW_CREDENTIAL";
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
        public static final int END_PAGE = 0;
    }

    public static class Application {
        // Probably also want to change User-Agent
        static final String APPLICATION_VERSION = "5.13.0";
        static final String USER_AGENT = "Argenta-PROD/3301 CFNetwork/1121.2.2 Darwin/19.3.0";
    }

    public static class Device {
        public static final String UNKNOWN = "N/A";
        public static final String VENDOR = "Apple";
        public static final String NAME = "Tink";
        public static final String MODEL = "iPhone9,4";
        public static final String OS_VERSION = "12.3.1";
        public static final String OS = "iOS";
    }

    public static final class Api {
        public static final String AUTH_METHOD_REGISTER = "REGISTREER";
        public static final String AUTH_METHOD_PIN = "PIN";
    }

    public static class ErrorResponse {
        public static final String AUTHENTICATION = "error.authentication";
        public static final String ERROR_CODE_SBB = "error.sbb";
        public static final String ERROR_INVALID_REQUEST = "error.invalid.request";
        public static final String TOO_MANY_DEVICES = "maximumaantal actieve registraties";
        public static final String TOO_MANY_ATTEMPTS =
                "je hebt te vaak een foute pincode ingevoerd";
        public static final String AUTHENTICATION_ERROR = "de logingegevens zijn niet juist";
        public static final String ACCOUNT_BLOCKED = "is geblokkeerd";
        public static final String PROBLEM_SOLVING_IN_PROGRESS =
                "We lossen het probleem zo snel mogelijk op";
        public static final String SOMETHING_WRONG = "er ging iets mis";
    }
}
