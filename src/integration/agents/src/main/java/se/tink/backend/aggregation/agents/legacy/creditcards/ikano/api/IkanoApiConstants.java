package se.tink.backend.aggregation.agents.creditcards.ikano.api;

public class IkanoApiConstants {

    public static final class ErrorCode {
        public static final String BANKID_IN_PROGRESS = "MOBILEBANKID_ALREADY_IN_PROGRESS";
        public static final String NO_CLIENT = "NO_CLIENT";
        public static final String BANKID_CANCELLED = "MOBILEBANKID_USER_CANCEL";
    }

    public class Error {
        public static final String TECHNICAL_ISSUES = "Tekniskt fel";
        public static final String WRONG_SSN =
                "Det angivna personnumret kunde inte identifieras, vänligen kontrollera personnumret och försök igen";
    }
}
