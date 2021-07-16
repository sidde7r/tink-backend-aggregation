package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

public class CommerzBankConstants {

    static class HeaderKeys {
        static final String X_REQUEST_ID = "X-Request-ID";
        static final String TPP_EXPLICIT_AUTHORISATION_PREFERRED =
                "TPP-Explicit-Authorisation-Preferred";
        static final String PSU_ID = "PSU-ID";
    }

    public static class ErrorMessages {
        public static final String MISSING_SCA_URL = "Missing sca redirect url.";
    }
}
