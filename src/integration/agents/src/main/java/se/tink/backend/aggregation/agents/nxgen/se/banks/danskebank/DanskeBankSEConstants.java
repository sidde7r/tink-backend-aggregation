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
}
