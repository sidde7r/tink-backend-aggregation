package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import java.util.UUID;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public final class WLConstants {
    private WLConstants() {
        throw new AssertionError();
    }

    public static final class Url {
        public static final String AUTHENTICATE = "/iphone/authenticate";
        public static final String J_SECURITY_CHECK = "/apps/services/j_security_check";
        public static final String INVOKE = "/invoke";
        // URLs including the module name
        public static final String API_ROOT = "/apps/services/api/"; // Note: trailing slash
        public static final String INIT = "/iphone/init";
        public static final String LOGOUT = "/iphone/logout";
        public static final String LOGIN = "/iphone/login";
        public static final String QUERY = "/iphone/query";
        public static final String HEARTBEAT = "/iphone/heartbeat";
    }

    public static class Headers {
        public static final String X_WL_APP_VERSION = "x-wl-app-version";
        public static final String WL_AUTHORIZATION_IN_BODY = "wl-authorization-in-body";
    }

    public static class Storage {
        public static final String WL_INSTANCE_ID = "WL-Instance-Id";
        public static final String KEY_PAIR = "KEY_PAIR";
        public static final String SHARED_AES_KEY = "SHARED_AES_KEY";
        public static final String SHARED_AES_IV = "SHARED_AES_IV";
    }

    public enum Procedure {
        fetch_FinancialStatusAccountDO,
        fetch_AdvisorsListDO,
        fetch_AccountDO
    }

    public static class Regex {
        public static final String INCORRECT_PASSWORD = ".*Z1110.*";
        public static final String ACCOUNT_LOCKED_TEMPORARILY = ".*Z9909.*";
        public static final String ACCOUNT_LOCKED_PERMANENTLY = ".*Z1159.*";
        public static final Pattern ENCLOSED_JSON =
                Pattern.compile("\\/\\*-secure-\\s*(.*)\\s*\\*\\/");
    }

    public static class Forms {
        public static final String REALM = "realm";
        public static final String REALM_VALUE = "AvantiSecureRealm";
        public static final String J_USERNAME = "j_username";
        public static final String J_PASSWORD = "j_password";
        public static final String USERNAME = "username";
        public static final String PX2 = "px2";
        public static final String SECP = "secP";
        public static final String SECP_VALUE =
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
        public static final String SECP_VALUE_SHORT = "06XXXX";
        public static final String ADAPTER = "adapter";
        public static final String ADAPTER_SECURITY_SERVICE = "UC_SecurityService_DE";
        public static final String ADAPTER_FACADE = "UC_Facade_DE";
        public static final String PROCEDURE = "procedure";
        public static final String PROCEDURE_LOGIN = "login";
        public static final String COMPRESS_RESPONSE = "compressResponse";
        public static final String PARAMETERS = "parameters";
        public static final String FETCH_CRYPT = "fetch_crypt";
    }

    public enum LogTags {
        WL_AUTHENTICATION_FEEDBACK,
        WL_AUTHENTICATION_NOT_SUCCESSFUL,
        WL_AUTHENTICATION_NOT_LEGIT,
        WL_RECEIVED_PLAINTEXT,
        WL_SENT_PLAINTEXT;

        public LogTag toTag() {
            return LogTag.from(name());
        }
    }

    public static final String DEVICE_ID = UUID.randomUUID().toString().toUpperCase();

    public static final String WL_APP_VERSION = "3.0.002";

    public static final String ENVIRONMENT = "iphone";
    public static final String MODEL = "iPhone9,3";
    public static final String OS = "10.1.1";

    public static final int RSA_KEY_SIZE = 512;

    public static final String ALG = "RS256";
}
