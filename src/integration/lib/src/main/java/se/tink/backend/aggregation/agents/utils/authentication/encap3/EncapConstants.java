package se.tink.backend.aggregation.agents.utils.authentication.encap3;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncapConstants {
    public static final String B64_ELLIPTIC_CURVE_PUBLIC_KEY =
            "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAHAk7SYMUWaKreRM0N7QB++IUoHP7bhNJRYeLZG3ANsvJhyhkB8soTniW9fzX0cmDmyl+wxuzqoIbfFd";

    // The storage keys are compatible with the old encap client.
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Storage {
        public static final String PERSISTENT_STORAGE_KEY = "__encapStorage";
        public static final String B64_DEVICE_HASH = "deviceHash";
        public static final String DEVICE_UUID = "deviceUUID";
        public static final String HARDWARE_ID = "hardwareId";
        public static final String B64_AUTHENTICATION_KEY = "b64AuthenticationKey";
        public static final String B64_AUTHENTICATION_KEY_WITHOUT_PIN =
                "b64AuthenticationKeyWithoutPin";
        public static final String B64_SALT_HASH = "saltHash";
        public static final String SALT_HASH_1 = "saltHash1";
        public static final String SIGNING_KEY_PHRASE = "signingKeyPhrase";
        public static final String REGISTRATION_ID = "registrationId";
        public static final String CLIENT_SALT_CURRENT_KEY = "clientSaltCurrentKey";
        public static final String CLIENT_SALT_CURRENT_KEY_ID = "clientSaltCurrentKeyId";
        public static final String USERNAME = "username";
        public static final String SAM_USERID = "samUserId";
        public static final String HW_KEY = "hwKey";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class MessageConstants {
        public static final String ACTIVATED_AUTH_METHODS = "activatedAuthMethods";
        public static final String APPLICATION_ID = "applicationId";
        public static final String B64_RESPONSE_CURRENT = "b64ResponseCurrent";
        public static final String CLIENT_DATA = "clientData";
        public static final String CLIENT_ONLY = "clientOnly";
        public static final String HEX_APN_TOKEN = "hexAPNToken";
        public static final String OPERATION = "operation";
        public static final String PURPOSE = "purpose";
        public static final String USED_AUTH_METHOD = "usedAuthMethod";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Urls {
        private static final String HOST = "https://bf-esb-internet.edb.com";
        private static final String SOAP_HOST = "https://nettbank.edb.com";

        public static final URL CRYPTO_EXCHANGE =
                new URL(HOST + "/samob/platform-smartdevice/client");
        public static final URL SOAP_AUTHENTICATION =
                new URL(SOAP_HOST + "/secesb/rest/sam-ws/SECSMobileAuthenticationService_V3_0");
        public static final URL SOAP_ACTIVATION =
                new URL(SOAP_HOST + "/secesb/rest/sam-ws/SECSMobileActivationService_V2_0");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HttpHeaders {
        public static final String EVRY_CLIENTNAME_KEY = "X-EVRY-CLIENT-CLIENTNAME";
        public static final String EVRY_CLIENTNAME_VALUE = "sam-client";
        public static final String EVRY_REQUESTID = "X-EVRY-CLIENT-REQUESTID";
        public static final String USER_AGENT_KEY = "User-Agent";
        public static final String USER_AGENT_VALUE = "SAM iOS";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Soap {
        public static final String EC_SUCCESS = "0";
        public static final String EC_ACTIVATION_TIMED_OUT = "222";
        public static final String EC_ACTIVATION_SESSION_IS_ALREADY_ACTIVATED = "250";
        public static final String EC_INVALID_USERNAME_OR_ACTIVATION_CODE = "251";
        public static final String HEADERS_B64 =
                "VXNlci1BZ2VudD1TQU0gaU9TDQpDb250ZW50LVR5cGU9YXBwbGljYXRpb24veC13d3ctZm9ybS11cmxlbmNvZGVkDQpDb250ZW50LUxlbmd0aD0xMjExDQo";
        public static final String MAC_B64 = "TUFDAQ==";
        public static final String ENC_B64 = "RU5DAQ==";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Message {
        public static final String APPLICATION_ID = "encap";
        public static final String HEX_APN_TOKEN = "";
        public static final String REGISTRATION_KEY = "registrationId";
        public static final String OPERATION_KEY = "operation";
        public static final String OPERATION_REGISTER = "REGISTER";
        public static final String OPERATION_IDENTIFY = "IDENTIFY";
        public static final String OPERATION_ACTIVATE = "ACTIVATE";
        public static final String OPERATION_AUTHENTICATE = "AUTHENTICATE";
        public static final String CLIENT_ONLY = "true";
        public static final String PURPOSE = "1";
        public static final String DEVICE_PIN = "DEVICE:PIN";
        public static final String DEVICE = "DEVICE";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class DeviceInformation {
        public static final String NAME = "Tink";
        public static final String SIGNER_HASHES = "";
        public static final String USER_INTERFACE_IDIOM = "0";
    }
}
