package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TriodosConstants {

    public static final String BASE_URL = "https://api-ma.triodos.com";
    public static final String AUTH_BASE_URL = "https://api.triodos.com";
    public static final Pattern HOLDER_NAME_SPLITTER =
            Pattern.compile(" E[NO] | EN?/OF? | OF ", Pattern.CASE_INSENSITIVE);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String CONSENT = "/xs2a-bg/nl/v1/consents";
        public static final String AUTHORIZE_CONSENT = CONSENT + "/%s/authorisations/%s";
        public static final String TOKEN = "/auth/nl/v1/token";
        public static final String AIS_BASE = "/xs2a-bg";
        public static final String ACCOUNTS = AIS_BASE + "/nl/v1/accounts";
        public static final String AUTH = "/auth/nl/v1/auth";
        public static final String CONSENT_STATUS = BASE_URL + CONSENT + "/{consent-id}/status";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PathParameterKeys {
        public static final String CONSENT_ID = "consent-id";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String AUTHORIZATION_ID = "authorizationId";
        public static final String CONSENT_STATUS = "consentStatus";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValues {
        public static final String DATE_FROM = "2000-10-10";
        public static final String SCOPE = "openid offline_access AIS:";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String AUTHORIZATION_ID = "authorizationId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
        public static final String TENANT = "nl";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CredentialKeys {
        public static final String IBANS = "ibans";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HttpClient {
        public static final int MAX_RETRIES = 2;
        public static final int RETRY_SLEEP_MILLISECONDS = 2000;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Oauth2Errors {
        public static final String CONSENT_REQUIRED = "consent_required";
        public static final String INVALID_REQUEST = "invalid_request";
        public static final String CANCELLED = "cancelled";
        public static final String NO_PENDING_AUTHORIZATIONS = "no pending authorisations found";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConsentErrors {
        public static final String FORMAT_ERROR = "FORMAT_ERROR";
        public static final String PRODUCT_INVALID = "PRODUCT_INVALID";
    }
}
