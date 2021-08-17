package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BelfiusConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessages {
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        public static final String BASE_URL = "https://psd2.b2b.belfius.be:8443";
        public static final String FETCH_ACCOUNT_PATH = BASE_URL + "/accounts/";
        public static final String FETCH_TRANSACTIONS_PATH =
                BASE_URL + "/accounts/{logical_id}/transactions";
        public static final String CONSENT_PATH = BASE_URL + "/consent-uris";
        public static final String TOKEN_PATH = BASE_URL + "/token";
        public static final String CREATE_PAYMENT = BASE_URL + "/payments/sepa-credit-transfers";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CODE = "code";
        public static final String ID_TOKEN = "id_token";
        public static final String LOGICAL_ID = "logical_id";
        public static final String SCA_TOKEN = "sca_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
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
        public static final String SCA_TOKEN = "SCA-Token";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryKeys {
        public static final String IBAN = "iban";
        public static final String STATE = "state";
        public static final String FROM_DATE = "date_from";
        public static final String TO_DATE = "date_to";
        public static final String NEXT = "next";
        public static final String SCOPE = "scope";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorCodes {
        public static final String ACCOUNT_NOT_SUPPORTED = "20003";
        public static final String NOT_SUPPORTED = "20004";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Errors {
        public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    }
}
