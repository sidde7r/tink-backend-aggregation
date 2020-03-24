package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

public class SdcNoConstants {
    public static class Authentication {
        public static final String IFRAME_BANKID_LOGIN_URL =
                "https://www.nettbankportal.no/{bankcode}/nettbank2/logon/bankidjs/";

        public static final String PORTALBANK_LOGIN_URL =
                "https://id.portalbank.no/wsl/slogin/Run?n_bank={bankcode}";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }
}
