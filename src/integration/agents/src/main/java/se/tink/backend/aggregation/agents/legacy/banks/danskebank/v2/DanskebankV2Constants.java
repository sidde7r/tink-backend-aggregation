package se.tink.backend.aggregation.agents.banks.danskebank.v2;

public class DanskebankV2Constants {
    public static class ErrorMessage {
        public static final String TECHNICAL_ERROR =
                "det har uppstått ett tekniskt fel. försök igen.";
        public static final String AUTHORIZATION_NOT_POSSIBLE = "Authorization not possible";
        public static final String UNAUTHORIZED =
                "the remote server returned an error: (401) unauthorized.";
        public static final String SESSION_EXPIRED =
                "your session was cut off as you have not used the system for a long time. please make a new logon.";
    }

    public static class ErrorCode {
        public static final int TECHNICAL_ERROR = 3;
        public static final int AUTHORIZATION_NOT_POSSIBLE = 333;
        public static final int UNAUTHORIZED = 6;
        public static final int INVALID_STATE = 9;
    }
}
