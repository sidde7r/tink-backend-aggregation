package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

public final class ErstebankConstants {

    public static final String INTEGRATION_NAME = "erstebank-at";

    private ErstebankConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_AUTH = "/sandbox-idp";
        public static final String AUTH = BASE_AUTH + "/auth";
        public static final String TOKEN = BASE_AUTH + "/token";
        public static final String ACCOUNTS = "/psd2-accounts-api/accounts";
        public static final String CONSENT = "/psd2-consent-api/consents";
        public static final String SIGN_CONSENT = "/psd2-consent-api/consents/%s/authorisations";
        public static final String TRANSACTIONS = "/psd2-accounts-api/accounts/%s/transactions";
    }
}
