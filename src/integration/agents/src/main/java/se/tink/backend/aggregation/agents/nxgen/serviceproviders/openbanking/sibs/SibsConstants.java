package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import se.tink.backend.aggregation.nxgen.http.URL;

public final class SibsConstants {

    private SibsConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String NO_BALANCE = "No balance found!";
        public static final String UNKNOWN_TRANSACTION_STATE = "Unknown transaction state.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://site1.sibsapimarket.com:8444/sibs/apimarket";

        public static final URL ACCOUNTS = new URL(BASE_URL + "/{aspsp-cde}/v1/accounts");
        public static final URL ACCOUNT_DETAILS =
                new URL(BASE_URL + "/{aspsp-cde}/v1/accounts/{account-id}");
        public static final URL CREATE_CONSENT = new URL(BASE_URL + "/{aspsp-cde}/v1/consents");
        public static final URL CONSENT_STATUS =
                new URL(BASE_URL + "/{aspsp-cde}/v1/consents/{consent-id}/status");
        public static final URL ACCOUNT_BALANCES =
                new URL(BASE_URL + "/{aspsp-cde}/v1/accounts/{account-id}/balances");
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(BASE_URL + "/{aspsp-cde}/v1/accounts/{account-id}/transactions");
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "CONSENT_ID";
    }

    public static class QueryKeys {

        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String WITH_BALANCE = "withBalance";
        public static final String PSU_INVOLVED = "psuInvolved";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_TO = "dateTo";
        public static final String DATE_FROM = "dateFrom";
    }

    public static class QueryValues {

        public static final String BOTH = "both";
    }

    public static class HeaderKeys {

        public static final String TPP_REQUEST_ID = "TPP-Request-ID";
        public static final String DATE = "Date";
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String SIGNATURE = "Signature";
        public static final String TPP_CERTIFICATE = "TPP-Certificate";
        public static final String TPP_TRANSACTION_ID = "TPP-Transaction-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String DIGEST = "Digest";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class FormValues {

        public static final String ALL_ACCOUNTS = "all-accounts";
        public static final Integer FREQUENCY_PER_DAY = 100;
    }

    public static class Formats {

        public static final String CONSENT_BODY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String CONSENT_REQUEST_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
        public static final String SHA_256 = "SHA-256";
        public static final String RSA = "RSA";
        public static final String PAGINATION_DATE_FORMAT = "yyyy-MM-dd";
        public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd";
        public static final String SIGNATURE_STRING_FORMAT =
                "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    }

    public static class HeaderValues {

        public static final String DIGEST_PREFIX = "SHA-256=";
    }

    public static class PathParameterKeys {

        public static final String ASPSP_CDE = "aspsp-cde";
        public static final String ACCOUNT_ID = "account-id";
        public static final String CONSENT_ID = "consent-id";
    }

    public static class SignatureKeys {

        public static final String KEY_ID = "keyId=";
        public static final String ALGORITHM = "algorithm=";
        public static final String HEADERS = "headers=";
        public static final String SIGNATURE = "signature=";
    }

    public static class SignatureValues {

        public static final String RSA_SHA256 = "rsa-sha256";
        public static final String HEADERS = "Digest TPP-Transaction-ID TPP-Request-ID Date";
        public static final String HEADERS_NO_DIGEST = "TPP-Transaction-ID TPP-Request-ID Date";
    }
}
