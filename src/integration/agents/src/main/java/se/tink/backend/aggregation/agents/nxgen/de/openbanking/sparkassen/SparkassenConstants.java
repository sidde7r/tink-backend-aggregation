package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SparkassenConstants {

    public static final String REGEX = "\\s*,\\s*";

    public static final String INTEGRATION_NAME = "sparkassen";

    private SparkassenConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String MISSING_SCA_AUTHORIZATION_URL = "Sca Authorization Url missing";
    }

    public static class Urls {
        public static final String BASE_URL = "https://xs2a.f-i-apim.de:8443/fixs2aop-env";
        public static final URL GET_CONSENT =
                new URL(BASE_URL + "/xs2a-api/{bankCode}/v1/consents");
        public static final URL AUTHORIZE = new URL(BASE_URL + "/oauth/{bankCode}/authorize");
        public static final URL UPDATE_SCA_METHOD =
                new URL(GET_CONSENT + "/{consentId}/authorisations/{authorizationId}");
        public static final URL FINALIZE_AUTHORIZATION =
                new URL(GET_CONSENT + "/{consentId}/authorisations/{authorizationId}");
        public static final URL FETCH_ACCOUNTS =
                new URL(BASE_URL + "/xs2a-api/{bankCode}/v1/accounts");
        public static final URL FETCH_BALANCES = new URL(FETCH_ACCOUNTS + "/{accountId}/balances");
        public static final URL FETCH_TRANSACTIONS =
                new URL(FETCH_ACCOUNTS + "/{accountId}/transactions");
    }

    public static class PathVariables {
        public static final String CONSENT_ID = "consentId";
        public static final String AUTHORIZATION_ID = "authorizationId";
        public static final String ACCOUNT_ID = "accountId";
        public static final String BANK_CODE = "bankCode";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "consentId";
        public static final String AUTHORIZATION_ID = "authorizationId";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String GRATNT_TYPE = "grant_type";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        public static final String PSU_ID = "PSU-ID";
    }

    public static class FormValues {
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String ALL_ACCOUNTS = "allAccounts";
    }

    public static class PollStatus {
        public static final String FINALISED = "finalised";
        public static final String FAILED = "failed";
    }

    public static class CredentialKeys {
        public static final String IBAN = "IBAN";
    }

    public static class XmlConstants {

        public static final String DBIT = "DBIT";
        public static final String BOOKED = "BOOK";
        public static final String DOCUMENTS_OPEN = "<Documents>";
        public static final String VERSION_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        public static final String XMLNS_ATRIBUTE =
                "xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02\"";
        public static final String XMLNS_ATRIBUTE_EXTENDED =
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:camt.052.001.02 camt.052.001.02.xsd\"";
        public static final String DOCUMENTS_CLOSED = "</Documents>";
    }
}
