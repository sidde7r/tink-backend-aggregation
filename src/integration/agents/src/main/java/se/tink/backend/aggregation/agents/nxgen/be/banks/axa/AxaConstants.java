package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.google.common.collect.ImmutableMap;

public final class AxaConstants {
    private AxaConstants() {
        throw new AssertionError();
    }

    public static class Url {
        private static String BASE = "https://mobile.axabank.be/";
        private static String AUTH_BASE = BASE + "AXA_BANK_TransmitApi/api/v2/auth/";

        static final String ANONYMOUS_INVOKE = AUTH_BASE + "anonymous_invoke?aid=mobile";
        static final String ASSERT = AUTH_BASE + "assert?aid=mobile";
        static final String BIND = AUTH_BASE + "bind?aid=mobile";
        static final String LOGIN = AUTH_BASE + "login?aid=mobile";

        public static final String LOGON = BASE + "AXA_BE_MOBILE_logon03";
        public static final String FETCH_ACCOUNTS = BASE + "AXA_BE_MOBILE_getAccounts01";
        public static final String FETCH_TRANSACTIONS = BASE + "AXA_BE_MOBILE_getAccountHistory01";
    }

    public static final ImmutableMap<String, Object> AUTH_HEADERS_JSON =
            ImmutableMap.<String, Object>builder()
                    .put("Host", "mobile.axabank.be")
                    .put("User-Agent", "axamobileiOS/2.30.7 (iPhone; iOS 13.3.1; Scale/2.00)")
                    .put("Content-Type", "application/json")
                    .put(
                            "Authorization",
                            "TSToken 3b20ff25-4b71-4736-8c72-7533f861e4e9; tid=mobile")
                    .put("X-TS-Client-Version", "3.6.13;[1,2,3,6,7,8,10,11,12,14,19]")
                    .build();

    public static final ImmutableMap<String, Object> HEADERS_JSON =
            ImmutableMap.<String, Object>builder()
                    .put("Host", "mobile.axabank.be")
                    .put("User-Agent", Request.USER_AGENT)
                    .put("Axa-Version", Request.AXA_VERSION)
                    .put("Content-Type", "application/json; charset=utf-8")
                    .build();

    public static final ImmutableMap<String, Object> HEADERS_FORM =
            ImmutableMap.<String, Object>builder()
                    .put("Host", "mobile.axabank.be")
                    .put("User-Agent", Request.USER_AGENT)
                    .put("Axa-Version", Request.AXA_VERSION)
                    .put("Content-Type", "application/x-www-form-urlencoded")
                    .build();

    public static class Request {
        public static final String TS_CLIENT_VERSION = "3.6.13;[1,2,3,6,7,8,10,11,12,14,19]";
        public static final String USER_AGENT = "AXA mobile API-v1.0 (axa-mobile-2.30)";
        public static final String APPL_CD = "MOBILEBANK";
        public static final String AXA_VERSION = "05";
        public static final String BASIC_AUTH =
                "NDNiNzQzMWMtZWI0Mi00ZWRjLTgxODYtNjczNzE4NDE5NDQ4OjdlMzk0NjliLTA1ZTQtNDk0OS1hZTMwLWNiOTUxOGZhYWRkYQ==";
        public static final String DIRECTION_FLAG = "1";
        public static final String REFERENCE_NUMBER = "";
        public static final String TRANSACTION_CODE = "";
        public static final String UPDATE_TIMESTAMP = "";
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
}
