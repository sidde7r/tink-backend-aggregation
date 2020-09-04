package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

public class DanskeBankSEConstants {

    public static class TransferType {
        public static final String INTERNAL = "TransferToOwnAccountSEv2";
        public static final String EXTERNAL = "TransferToOtherAccountSE";
    }

    public static class TransferAccountType {
        public static final String INTERNAL = "internal";
        public static final String EXTERNAL = "external";
    }

    public static class TransferConfig {
        public static final int SOURCE_MESSAGE_MAX_LENGTH = 19;
        public static final int DESTINATION_MESSAGE_MAX_LENGTH = 12;
        public static final String WHITE_LISTED_CHARACTER_STRING = ",._-?!/:()&`~";
    }
}
