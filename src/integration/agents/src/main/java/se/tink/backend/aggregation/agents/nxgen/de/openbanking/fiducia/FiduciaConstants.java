package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class FiduciaConstants {

    public static final String INTEGRATION_NAME = "fiducia";

    private FiduciaConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://xs2a-test.fiduciagad.de/xs2a";

        public static final URL CREATE_CONSENT = new URL(BASE_URL + ApiServices.CREATE_CONSENT);
        public static final URL AUTHORIZE_CONSENT =
                new URL(BASE_URL + ApiServices.AUTHORIZE_CONSENT);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiServices.GET_ACCOUNTS);
        public static final URL GET_BALANCES = new URL(BASE_URL + ApiServices.GET_BALANCES);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiServices.GET_TRANSACTIONS);
    }

    public static class ApiServices {
        public static final String CREATE_CONSENT = "/v1/consents";
        public static final String AUTHORIZE_CONSENT = "/v1/consents/{consentId}/authorisations";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_BALANCES = "/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consent-id";
    }

    public static class QueryKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DATE = "Date";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String SIGNATURE = "Signature";
        public static final String DIGEST = "Digest";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_ID = "PSU-ID";
    }

    public static class HeaderValues {
        public static final String CONSENT_VALID = "CONSENTVALID";
    }

    public static class FormValues {
        public static final String FREQUENCY_PER_DAY = "4";
        public static final String TRUE = "true";
        public static final String VALID_UNTIL = "2019-11-11";
        public static final String FALSE = "false";
    }

    public static class SignatureKeys {
        public static final String KEY_ID = "keyId=\"";
        public static final String ALGORITHM = "\",algorithm=\"";
        public static final String HEADERS = "\", headers=\"";
        public static final String SIGNATURE = "\", signature=\"";
        public static final String X_REQUEST_ID = "x-request-id: ";
        public static final String DIGEST = "digest: ";
        public static final String DATE = "date: ";
        public static final String PSU_ID = "psu-id: ";
        public static final String EMPTY = "";
        public static final String SHA_256 = "SHA-256=";
    }

    public static class SignatureValues {
        public static final String ALGORITHM = "SHA256withRSA";
        public static final String HEADERS_WITH_PSU_ID = "x-request-id digest date psu-id";
        public static final String HEADERS = "x-request-id digest date";
        public static final String EMPTY_BODY = "";
    }

    public static class BalanceTypes {
        public static final String AVAILABLE = "available";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
        public static final String PSU_ID = "psu-id";
        public static final String PASSWORD = "password";
    }
}
