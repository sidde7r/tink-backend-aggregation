package se.tink.backend.aggregation.agents.creditcards.ikano.api;

public class IkanoApiConstants {

    public static final class Endpoints {
        public static final String ROOT_URL = "https://partner.ikanobank.se/mCommunicationService/";
        public static final String MOBILE_BANK_ID_REFERENCE_URI = "MobileBankIdReference/";
        public static final String MOBILE_BANK_ID_SESSION_URI = "MobileBankIdSession/";
        public static final String CARDS_URI = "Cards/ALL/";
        public static final String REGISTER_CARDS_URI = "RegisteredCards/";
        public static final String ENGAGEMENTS_URI =
                "Engagements/ALL/?numofbonustrans=1&numoftrans=";
    }

    public static final class HeaderKeys {
        public static final String USERNAME = "Username";
        public static final String SESSION_ID = "SessionId";
        public static final String SESSION_KEY = "SessionKey";
        public static final String USER_AGENT = "User-Agent";
        public static final String DEVICE_ID = "DeviceId";
        public static final String DEVICE_AUTH = "DeviceAuth";
    }

    public static final class QueryValues {
        public static final int DEFAULT_LIMIT = 200;
    }

    public static final class ErrorCode {
        public static final String BANKID_IN_PROGRESS = "MOBILEBANKID_ALREADY_IN_PROGRESS";
        public static final String NO_CLIENT = "NO_CLIENT";
        public static final String BANKID_CANCELLED = "MOBILEBANKID_USER_CANCEL";
        public static final String BANKID_UNKNOWN_ERROR = "MOBILEBANKID_UNKNOWN_ERROR";
    }

    public static final class Error {
        public static final String TECHNICAL_ISSUES = "Tekniskt fel";
        public static final String WRONG_SSN =
                "Det angivna personnumret kunde inte identifieras, vänligen kontrollera personnumret och försök igen";
    }
}
