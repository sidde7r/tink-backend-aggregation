package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

public final class BelfiusConstants {

    private BelfiusConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        private ErrorMessages() {}

        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    public static class Urls {
        private Urls() {}

        public static final String FETCH_ACCOUNT_PATH = "/accounts/";
        public static final String FETCH_TRANSACTIONS_PATH = "/accounts/{logical_id}/transactions";
        public static final String CONSENT_PATH = "/consent-uris";
        public static final String TOKEN_PATH = "/token";
        public static final String CREATE_PAYMENT = "/payments/sepa-credit-transfers";
    }

    public static class StorageKeys {
        private StorageKeys() {}

        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CODE = "code";
        public static final String ID_TOKEN = "id_token";
        public static final String LOGICAL_ID = "logical_id";
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String REQUEST_ID = "Request-ID";
        public static final String ACCEPT = "Accept";
        public static final String CLIENT_ID = "Client-ID";
        public static final String REDIRECT_URI = "redirect-uri";
        public static final String CODE_CHALLENGE = "Code-Challenge";
        public static final String CODE_CHALLENGE_METHOD = "Code-Challenge-Method";
        public static final String SIGNATURE = "Signature";
        public static final String LOCATION = "Location";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class FormKeys {
        private FormKeys() {}

        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        private FormValues() {}

        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class HeaderValues {
        private HeaderValues() {}

        public static final String BASE_ACCEPT = "application/vnd.belfius.api+json; version=";
        public static final String CONSENT_VERSION = "2";
        public static final String TOKEN_VERSION = "1.1";
        public static final String ACCOUNT_VERSION = "1";
        public static final String TRANSACTION_VERSION = "1.2";
        public static final String PAYMENT_VERSION = "1";
        public static final String CONSENT_ACCEPT = BASE_ACCEPT + CONSENT_VERSION;
        public static final String TOKEN_ACCEPT = BASE_ACCEPT + TOKEN_VERSION;
        public static final String ACCOUNT_ACCEPT = BASE_ACCEPT + ACCOUNT_VERSION;
        public static final String TRANSACTION_ACCEPT = BASE_ACCEPT + TRANSACTION_VERSION;
        public static final String PAYMENT_ACCEPT = BASE_ACCEPT + PAYMENT_VERSION;
        public static final String ACCEPT_LANGUAGE = "fr";
        public static final String CODE_CHALLENGE_TYPE = "S256";
        public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    }

    public static class CredentialKeys {
        private CredentialKeys() {}

        public static final String IBAN = "iban";
    }

    public static class QueryKeys {
        private QueryKeys() {}

        public static final String IBAN = "iban";
        public static final String STATE = "state";
        public static final String FROM_DATE = "date_from";
        public static final String TO_DATE = "date_to";
        public static final String NEXT = "next";
        public static final String SCOPE = "scope";
    }

    public static class ErrorCodes {
        public static final String NOT_SUPPORTED = "20004";
    }
}
