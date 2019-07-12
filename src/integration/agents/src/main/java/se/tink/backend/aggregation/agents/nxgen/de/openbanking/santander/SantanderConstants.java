package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SantanderConstants {

    public static final String INTEGRATION_NAME = "santander";

    private SantanderConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final URL TOKEN = new URL(Endpoints.BASE_URL + Endpoints.TOKEN);
        public static final URL CONSENT = new URL(Endpoints.BASE_URL + Endpoints.CONSENT);
        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.BASE_AIS);
    }

    public static class Endpoints {
        public static final String BASE_URL = "https://apigateway-sandbox.api.santander.de";
        public static final String TOKEN = "/scb-openapis/sx/oauthsos/password/token";
        public static final String CONSENT = "/scb-openapis/sx/v1/consents";
        public static final String ACCOUNTS = "/scb-openapis/sx/v1/accounts";
        public static final String BASE_AIS = "/scb-openapis/sx";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consentId";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grant_type";
    }

    public static class QueryValues {
        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class HeaderValues {
        public static final String X_REQUEST_ID = "12345";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
