package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import java.util.regex.Pattern;

public final class TriodosConstants {

    private TriodosConstants() {
        throw new AssertionError();
    }

    public static final String BASE_URL = "https://api-ma.triodos.com";
    public static final String AUTH_BASE_URL = "https://api.triodos.com";
    public static final String PSU_IPADDRESS = "0.0.0.0";
    public static final Pattern HOLDER_NAME_SPLITTER =
            Pattern.compile(" E[NO] | EN?/OF? | OF ", Pattern.CASE_INSENSITIVE);

    public static class Urls {
        public static final String CONSENT = "/xs2a-bg/nl/v1/consents";
        public static final String AUTHORIZE_CONSENT = CONSENT + "/%s/authorisations/%s";
        public static final String TOKEN = "/auth/nl/v1/token";
        public static final String AIS_BASE = "/xs2a-bg";
        public static final String ACCOUNTS = AIS_BASE + "/nl/v1/accounts";
        public static final String AUTH = "/auth/nl/v1/auth";
    }

    public static class StorageKeys {
        public static final String AUTHORIZATION_ID = "authorizationId";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
    }

    public static class QueryValues {
        public static final String DATE_FROM = "2000-10-10";
        public static final String SCOPE = "openid offline_access AIS:";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION_ID = "authorizationId";
    }

    public static class HeaderValues {
        public static final String TENANT = "nl";
    }

    public static class CredentialKeys {
        public static final String IBANS = "ibans";
    }
}
