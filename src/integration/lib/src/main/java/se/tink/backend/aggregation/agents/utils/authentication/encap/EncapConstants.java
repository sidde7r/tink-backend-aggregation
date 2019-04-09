package se.tink.backend.aggregation.agents.utils.authentication.encap;

import se.tink.backend.aggregation.nxgen.http.URL;

public class EncapConstants {
    static final String B64_ELLIPTIC_CURVE_PUBLIC_KEY =
            "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAHAk7SYMUWaKreRM0N7QB++IUoHP7bhNJRYeLZG3ANsvJhyhkB8soTniW9fzX0cmDmyl+wxuzqoIbfFd";

    public static final class Urls {
        private static final String HOST = "https://bf-esb-internet.edb.com";
        private static final String SOAP_BASE = "/ws_proxy/soap";

        static final URL CRYPTO_EXCHANGE = new URL(HOST + "/samob/platform-smartdevice/client");
        static final URL PLAIN_TEXT_EXCHANGE = new URL(HOST + "/samob/platform-ios/client");
        static final URL AUTHENTICATION_SESSION_CREATE =
                new URL(HOST + SOAP_BASE + "/SECSMobileAuthenticationSessionCreate_V1_0Service");
        static final URL ACTIVATION_SESSION_UPDATE =
                new URL(HOST + SOAP_BASE + "/SECSMobileActivationSessionUpdate_V1_0Service");
        static final URL ACTIVATION_SERVICE =
                new URL(HOST + SOAP_BASE + "/SECSMobileActivationService_V1_0");
        static final URL USER_CREATE =
                new URL(HOST + SOAP_BASE + "/SECSMobileUserCreate_V1_0Service");
        static final URL AUTHENTICATION_SERVICE =
                new URL(HOST + SOAP_BASE + "/SECSMobileAuthenticationService_V1_0");
        static final URL AUTHENTICATION_SESSION_READ_SERVICE =
                new URL(HOST + SOAP_BASE + "/SECSMobileAuthenticationSessionRead_V1_0Service");
    }

    public static final class HttpHeaders {
        static final String AUTHENTICATION_SESSION_CREATE =
                "sECSMobileAuthenticationSessionCreate_V1_0";
        static final String ACTIVATION_SESSION_UPDATE = "sECSMobileActivationSessionUpdate_V1_0";
        static final String USER_CREATE = "sECSMobileUserCreate_V1_0";
        static final String AUTHENTICATION_SESSION_READ =
                "sECSMobileAuthenticationSessionRead_V1_0";
    }

    public static final class Storage {
        static final String ENCAP_STORAGE = "__encapStorage";
        static final String B64_APPLICATION_HASH = "applicationHash";
        static final String B64_DEVICE_HASH = "deviceHash";
        static final String DEVICE_UUID = "deviceUUID";
        static final String HARDWARE_ID = "hardwareId";
        static final String B64_AUTHENTICATION_KEY = "b64AuthenticationKey";
        static final String B64_AUTHENTICATION_KEY_WITHOUT_PIN = "b64AuthenticationKeyWithoutPin";
        static final String B64_TOTP_KEY = "b64TotpKey";
        static final String B64_SALT_HASH = "saltHash";
        static final String SIGNING_KEY_PHRASE = "signingKeyPhrase";
        static final String REGISTRATION_ID = "registrationId";
        static final String CLIENT_SALT_CURRENT_KEY = "clientSaltCurrentKey";
        static final String CLIENT_SALT_CURRENT_KEY_ID = "clientSaltCurrentKeyId";
        static final String B64_CHALLENGE_RESPONSE = "b64ChallengeResponse";
        static final String B64_CHALLENGE_RESPONSE_WITHOUT_PIN = "b64ChallengeResponseWithoutPin";
        static final String B64_RESPONSE_CURRENT = "b64ResponseCurrent";
        static final String B64_RESPONSE_CURRENT_WITHOUT_PIN = "b64ResponseCurrentWithoutPin";
        static final String SAM_USERID = "samUserId";
        static final String APPLICATION_VERSION = "applicationVersion";
        static final String ENCAP_API_VERSION = "encapApiVersion";
    }

    public static final class DeviceInformation {
        static final String MANUFACTURER = "Apple";
        public static final String MODEL = "iPhone8,1";
        public static final String NAME = "Tink";
        public static final String IS_ROOT_AVAILABLE = "false";
        static final String SIGNER_HASHES = "";
        public static final String OS_NAME_AND_TYPE = "iOS";
        public static final String SYSTEM_VERSION = "10.2";
        public static final String USER_INTERFACE_IDIOM = "0";
    }

    public static final class MessageInformation {
        public static final String APPLICATION_ID = "encap";
        public static final String HEX_APN_TOKEN = "";
        public static final String OPERATION_REGISTER = "REGISTER";
        public static final String OPERATION_IDENTIFY = "IDENTIFY";
        public static final String OPERATION_ACTIVATE = "ACTIVATE";
        public static final String OPERATION_AUTHENTICATE = "AUTHENTICATE";
        public static final String REQUIRE_TOKEN = "true";
        public static final String CLIENT_ONLY = "true";
        static final String PURPOSE = "1";
        static final String DEVICE_PIN = "DEVICE:PIN";
    }
}
