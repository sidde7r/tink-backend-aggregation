package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.agents.rpc.AccountTypes;

public class ArgentaConstants {

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "CHECKING")
                    .put(AccountTypes.SAVINGS, "SAVINGS")
                    .build();

    public static final class Url {
        private static final String BASE = "https://mobile-api.argenta.be/";
        static final URL KEEP_ALIVE_URL = new URL(BASE + "authentication/keep-alive");
        public static final URL AUTH_START = new URL(BASE + "authentication/start-auth");
        static final URL AUTH_VALIDATE = new URL(BASE + "authentication/validate-auth");
        static final URL ACCOUNTS = new URL(BASE + "accounts");
        static final String TRANSACTIONS = "/transactions";
        static final URL CREDIT_CARD = new URL(BASE + "cards");
    }

    public static final ImmutableMap<String, String> HEADERS =
            ImmutableMap.<String, String>builder()
                    .put("App-Version", Application.APPLICATION_VERSION)
                    .put("appVersion", Application.APPLICATION_VERSION)
                    .put("language", "NL")
                    .put("Accept-Language", "nl")
                    .put("Connection", "keep-alive")
                    .put("User-Agent", "Argenta-PROD/1136 CFNetwork/889.9 Darwin/17.2.0")
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
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
        public static final int END_PAGE = 0;
    }

    public static final class MultiFactorAuthentication {
        public static final String CODE = "code";
    }

    public static class Application {
        // Probably also want to change User-Agent
        public static final String APPLICATION_VERSION = "4.22.0";
    }

    public static class Device {
        public static final String VENDOR = "Apple";
        public static final String NAME = "Tink";
        public static final String MODEL = "iPhone 7";
        public static final String OS_VERSION = "11.0";
        public static final String OS = "iOS";
    }

    public static final class Api {
        public static final String AUTH_METHOD_REGISTER = "REGISTREER";
        public static final String AUTH_METHOD_PIN = "PIN";
    }

    public static class ErrorResponse {
        public static final String AUTHENTICATION = "error.authentication";
        public static final String ERROR_CODE_SBP = "error.sbp";
        public static final String TOO_MANY_DEVICES = "maximumaantal actieve registraties";
        public static final String AUTHENTICATION_ERROR = "de logingegevens zijn niet juist";
        public static final String ACCOUNT_BLOCKED = "is geblokkeerd";

    }

    public static class LogTags {
        public static final LogTag LOG_TAG_ACCOUNT = LogTag.from("#Argenta_account");
    }
}
