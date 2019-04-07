package se.tink.backend.aggregation.agents.utils.authentication.encap2;

import se.tink.backend.aggregation.nxgen.http.URL;

public class EncapConstants {
    public static final String B64_ELLIPTIC_CURVE_PUBLIC_KEY =
            "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAHAk7SYMUWaKreRM0N7QB++IUoHP7bhNJRYeLZG3ANsvJhyhkB8soTniW9fzX0cmDmyl+wxuzqoIbfFd";

    // The storage keys are compatible with the old encap client.
    public static final class Storage {
        public static final String PERSISTENT_STORAGE_KEY = "__encapStorage";
        public static final String B64_DEVICE_HASH = "deviceHash";
        public static final String DEVICE_UUID = "deviceUUID";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String B64_AUTHENTICATION_KEY = "b64AuthenticationKey";
        public static final String B64_AUTHENTICATION_KEY_WITHOUT_PIN =
                "b64AuthenticationKeyWithoutPin";
        public static final String B64_TOTP_KEY = "b64TotpKey";
        public static final String B64_SALT_HASH = "saltHash";
        public static final String SIGNING_KEY_PHRASE = "signingKeyPhrase";
        public static final String REGISTRATION_ID = "registrationId";
        public static final String CLIENT_SALT_CURRENT_KEY = "clientSaltCurrentKey";
        public static final String CLIENT_SALT_CURRENT_KEY_ID = "clientSaltCurrentKeyId";
        public static final String USERNAME = "username";
        public static final String SAM_USERID = "samUserId";
    }

    public static final class Urls {
        private static final String HOST = "https://bf-esb-internet.edb.com";
        private static final String SOAP_BASE = "/ws_proxy/soap";

        public static final URL CRYPTO_EXCHANGE =
                new URL(HOST + "/samob/platform-smartdevice/client");
        public static final URL AUTHENTICATION_SESSION_CREATE =
                new URL(HOST + SOAP_BASE + "/SECSMobileAuthenticationSessionCreate_V1_0Service");
        public static final URL ACTIVATION_SESSION_UPDATE =
                new URL(HOST + SOAP_BASE + "/SECSMobileActivationSessionUpdate_V1_0Service");
        public static final URL ACTIVATION_SERVICE =
                new URL(HOST + SOAP_BASE + "/SECSMobileActivationService_V1_0");
        public static final URL AUTHENTICATION_SERVICE =
                new URL(HOST + SOAP_BASE + "/SECSMobileAuthenticationService_V2_0");
    }

    public static final class HttpHeaders {
        public static final String AUTHENTICATION_SESSION_CREATE =
                "sECSMobileAuthenticationSessionCreate_V1_0";
        public static final String ACTIVATION_SESSION_UPDATE =
                "sECSMobileActivationSessionUpdate_V1_0";
    }

    public static final class Soap {
        public static final String EC_SUCCESS = "0";
        public static final String EC_INVALID_USERNAME_OR_ACTIVATION_CODE = "251";
    }

    public static final class Message {
        public static final String APPLICATION_ID = "encap";
        public static final String HEX_APN_TOKEN = "";
        public static final String OPERATION_REGISTER = "REGISTER";
        public static final String OPERATION_IDENTIFY = "IDENTIFY";
        public static final String OPERATION_ACTIVATE = "ACTIVATE";
        public static final String OPERATION_AUTHENTICATE = "AUTHENTICATE";
        public static final String REQUIRE_TOKEN = "true";
        public static final String CLIENT_ONLY = "true";
        public static final String PURPOSE = "1";
        public static final String DEVICE_PIN = "DEVICE:PIN";
        public static final String DEVICE = "DEVICE";
    }

    public static final class DeviceInformation {
        public static final String NAME = "Tink";
        public static final String IS_ROOT_AVAILABLE = "false";
        public static final String SIGNER_HASHES = "";
        public static final String USER_INTERFACE_IDIOM = "0";
    }
}
