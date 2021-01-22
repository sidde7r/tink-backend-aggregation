package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class ArgentaConstants {

    private ArgentaConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_SCA_METHOD = "The chosen SCA method is not valid";
    }

    public static class Urls {
        private static final String BASE_API_URL = "https://api.payments.argenta.be";
        public static final String BASE_BERLIN_GROUP = BASE_API_URL + "/berlingroup";
        public static final String BASE_AUTH_URL = "https://login.payments.argenta.be";
        public static final URL AUTHORIZATION = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZATION);
        public static final URL CONSENT = new URL(BASE_BERLIN_GROUP + Endpoints.CONSENT);
        public static final URL TOKEN = new URL(BASE_API_URL + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(BASE_BERLIN_GROUP + Endpoints.ACCOUNTS);
    }

    public static class Endpoints {
        public static final String CONSENT = "/v1/consents";
        public static final String AUTHORIZATION = "/psd2/v1/berlingroup-auth/authorise";
        public static final String TOKEN = "/psd2/v1/berlingroup-auth/token";

        public static final String ACCOUNTS = "/v1/accounts";
        public static final String TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
    }

    public static class PathVariables {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CHOSEN_SCA_METHOD = "chosenScaMethod";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String S256 = "S256";
        public static final String CODE = "code";
        public static final String SCOPE = "AIS:%s";
        public static final String BOTH = "both";
        public static final String START_DATE = "1970-01-01";
    }

    public static class HeaderKeys {
        public static final String DIGEST = "digest";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String DATE = "Date";
        public static final String SIGNATURE = "Signature";
        public static final String API_KEY = "apiKey";
        public static final String CERTIFICATE = "tpp-signature-certificate";
        public static final String CONSENT_ID = "Consent-Id";
        public static final String PSU_ID_ADDRESS = "psu-ip-address";
    }

    public static class HeaderValues {
        public static final String SHA_256 = "SHA-256=";
        public static final String SIGNATURE_HEADER =
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
        public static final String JSON_UTF_8 = "application/json;charset=UTF-8";
    }

    public enum HeadersToSign {
        DIGEST("digest"),
        X_REQUEST_ID("x-request-id");

        private String header;

        HeadersToSign(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final long NUMBER_OF_VALID_DAYS = 90L;
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class Formats {
        public static final String HEADER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss O";
        public static final String RESPONSE_DATE_FORMAT = "yyyy-MM-dd";
        public static final String CURRENCY = "EUR";
    }

    public static class BalanceTypes {
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
