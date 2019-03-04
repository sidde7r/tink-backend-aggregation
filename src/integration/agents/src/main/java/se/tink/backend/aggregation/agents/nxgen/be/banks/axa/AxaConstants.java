package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.Form;

public final class AxaConstants {
    private AxaConstants() {
        throw new AssertionError();
    }

    public static class Url {
        private static String BASE = "https://esg.services.axabank.be/";
        public static final String GENERATE_CHALLENGE = BASE + "AXA_BE_MOBILE_generateUCRChallenge";
        public static final String GENERATE_OTP_CHALLENGE =
                BASE + "AXA_BE_MOBILE_generateOTPChallenge";
        public static final String LOGON = BASE + "AXA_BE_MOBILE_logon";
        public static final String REGISTER_USER = BASE + "AXA_BE_MOBILE_registerUser";
        public static final String STORE_DERIVATION_CD = BASE + "AXA_BE_MOBILE_storeDerivationCd";
        public static final String PENDING_PRODUCT_REQUESTS =
                BASE + "AXA_BE_MOBILE_getPendingProductRequests01";
        public static final String FETCH_ACCOUNTS =
                BASE + "AXA_BE_MOBILE_getAccounts01";
    }

    public enum Storage {
        ACCESS_TOKEN,
        BASIC_AUTH,
        CLIENT_INITIAL_VECTOR_DECRYPT,
        CLIENT_INITIAL_VECTOR_INIT,
        DERIVATION_CODE,
        DEVICE_ID,
        DIGIPASS,
        DIGI_OTP_CHALLENGE_RESPONSE,
        ENCRYPTED_NONCES,
        ENCRYPTED_PUBLIC_KEY_AND_NONCE,
        ENCRYPTED_SERVER_NONCE,
        ENCRYPTED_SERVER_PUBLIC_KEY,
        GENERATE_OTP_CHALLENGE_RESPONSE,
        LOGON_RESPONSE,
        REGISTER_CHALLENGE,
        SERIAL_NO,
        SERVER_INITIAL_VECTOR,
        STORE_REGISTRATION_CD_RESPONSE,
        XFAD,
        ;
    }

    public static final ImmutableMap<String, Object> HEADERS_JSON =
            ImmutableMap.<String, Object>builder()
                    .put("Host", "esg.services.axabank.be")
                    .put("User-Agent", Request.USER_AGENT)
                    .put("Axa-Version", Request.AXA_VERSION)
                    .put("Content-Type", "application/json; charset=utf-8")
                    .build();
    public static final ImmutableMap<String, Object> HEADERS_FORM =
            ImmutableMap.<String, Object>builder()
                    .put("Connection", "keep-alive")
                    .put("Accept-Encoding", "gzip, deflate")
                    .put("Host", "esg.services.axabank.be")
                    .put("Accept", "*/*")
                    .put("User-Agent", Request.USER_AGENT)
                    .put("Axa-Version", Request.AXA_VERSION)
                    .put("Content-Type", "application/x-www-form-urlencoded")
                    .build();

    public static final ImmutableList<String> CIPHER_SUITES =
            ImmutableList.<String>builder()
                    .add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384")
                    .add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384")
                    .add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256")
                    .add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")
                    .add("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384")
                    .add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384")
                    .add("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256")
                    .add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256")
                    .add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384")
                    .add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384")
                    .add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA")
                    .add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA")
                    .add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256")
                    .add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256")
                    .add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA")
                    .add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA")
                    .add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256")
                    .add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256")
                    .add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA")
                    .add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA")
                    .add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256")
                    .add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256")
                    .add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA")
                    .add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA")
                    .add("TLS_RSA_WITH_AES_256_GCM_SHA384")
                    .add("TLS_RSA_WITH_AES_128_GCM_SHA256")
                    .add("TLS_RSA_WITH_AES_256_CBC_SHA256")
                    .add("TLS_RSA_WITH_AES_128_CBC_SHA256")
                    .add("TLS_RSA_WITH_AES_256_CBC_SHA")
                    .add("TLS_RSA_WITH_AES_128_CBC_SHA")
                    .add("TLS_EMPTY_RENEGOTIATION_INFO_SCSV")
                    .build();

    public static class Request {

        public static final String USER_AGENT = "AXA mobile API-v1.0 (axa-mobile-2.23)";
        public static final String APPL_CD = "MOBILEBANK";
        public static final String VERSION_NUMBER = "2.23";
        public static final String OPERATING_SYSTEM = "11.1.1";
        public static final String MODEL = "Tinkdevice";
        public static final String BRAND = "Tink";
        public static final String LANGUAGE = "de";
        public static final String AXA_VERSION = "05";
        public static final boolean JAILBROKEN_OR_ROOTED = false;
        public static final String GRANT_TYPE = "password";
        public static final String SCOPE = "mobilebanking";
        public static final Form LOGON_BODY =
                Form.builder()
                        .put("grant_type", AxaConstants.Request.GRANT_TYPE)
                        .put("scope", AxaConstants.Request.SCOPE)
                        .put("language", AxaConstants.Request.LANGUAGE)
                        .put("jailBrokenOrRooted", "" + AxaConstants.Request.JAILBROKEN_OR_ROOTED)
                        .put("versionNumber", AxaConstants.Request.VERSION_NUMBER)
                        .put("operatingSystem", AxaConstants.Request.OPERATING_SYSTEM)
                        .put("model", AxaConstants.Request.MODEL)
                        .put("brand", AxaConstants.Request.BRAND)
                        .put("applCd", AxaConstants.Request.APPL_CD)
                        .build();
        public static final String USERNAME_KEY = "username";
        public static final String PASSWORD_KEY = "password";
        public static final String DEVICEID_KEY = "deviceId";
        public static final String BASIC_AUTH =
                "NDNiNzQzMWMtZWI0Mi00ZWRjLTgxODYtNjczNzE4NDE5NDQ4OjdlMzk0NjliLTA1ZTQtNDk0OS1hZTMwLWNiOTUxOGZhYWRkYQ==";
    }

    public static class Response {
        // "Sie haben die maximale Anzahl an Registrierungen erreicht. Nehmen Sie bitte mit dem
        // Contact Center (03/286.66.56)"
        public static final String DEVICES_LIMIT_REACHED_CODE = "MB0027";

        // "Sie haben eine falsche Kartennummer eingegeben. Bitte, kontrollieren Sie die Daten Ihrer
        // AXA Bankkarte und versuchen Sie es nochmals, oder kontaktieren Sie das Contact Center
        // (03/286.66.56)."
        public static final String INCORRECT_CARD_NUMBER_CODE = "HB1004";

        // "Sie sind nicht als aktiever Homebanking Kunde bekannt."
        public static final String NOT_AN_ACTIVE_BANK_USER = "HB0044";

        // Ref D_50: Der eingegebene 'RESPONSE' Code ist nicht korrekt. Bitte versuchen Sie es noch
        // einmal. Achtung: Aus Sicherheitsgründen wird die Anzahl der Versuche auf 5 begrenzt.
        // ^--- this indicates that the entered challenge response is incorrect
        public static final String INCORRECT_CHALLENGE_RESPONSE_SUBSTRING = "nicht korrekt";

        public static final String BLOCKED_SUBSTRING = "Dieser Benutzer wird endgültig gesperrt";
    }

    public static class MultiFactorAuthentication {
        public static final String CODE = "code";
    }

    public enum LogTags {
        PERSISTENT_STORAGE;

        public LogTag toTag() {
            return LogTag.from(name());
        }
    }
}
