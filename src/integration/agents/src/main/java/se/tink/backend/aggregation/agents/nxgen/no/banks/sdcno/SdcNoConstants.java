package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

public class SdcNoConstants {
    public static class Authentication {
        public static final String NETTBANK_BANKID_LOGIN_URL =
                "https://www.nettbankportal.no/{bankcode}/nettbank2/logon/bankidjs/?portletname=bankidloginjs&portletaction=openmobilelogin";

        public static final String PORTALBANK_LOGIN_URL =
                "https://id.portalbank.no/wsl/slogin/Run?n_bank={bankcode}";

        public static final String EIKA_LOGIN_URL =
                "https://id.portalbank.no/web-kundeid/webresources/identifiser/eika/0770?returnUrl=https%3a%2f%2feika.no%2flogin%3freturnUrl%3d%2foversikt";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }
}
