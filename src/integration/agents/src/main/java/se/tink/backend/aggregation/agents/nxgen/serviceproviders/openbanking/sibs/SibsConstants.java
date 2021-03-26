package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

public final class SibsConstants {

    private static final String API_VERSION = "v1-0-2";

    private SibsConstants() {}

    public static class ErrorMessages {
        public static final String NO_BALANCE = "No balance found!";
        public static final String UNKNOWN_TRANSACTION_STATE = "Unknown transaction state.";
        public static final String MISSING_LINKS_OBJECT = "Response is missing links object";
        public static final String MISSING_PAGINATION_KEY = "Missing pagination key";
    }

    public static class Urls {
        public static final String BASE_URL = "https://site1.sibsapimarket.com/sibs/apimarket";
        private static final String BASE_URL_WITH_ASPSP_CODE_AND_VERSION =
                BASE_URL + "/{aspsp-cde}/" + API_VERSION;
        public static final String ACCOUNTS = BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/accounts";
        static final String CREATE_CONSENT = BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/consents";
        public static final String CONSENT_STATUS =
                BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/consents/{consent-id}/status";
        static final String ACCOUNT_BALANCES =
                BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/accounts/{account-id}/balances";
        public static final String ACCOUNT_TRANSACTIONS =
                BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/accounts/{account-id}/transactions";
        static final String PAYMENT_INITIATION =
                BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/payments/{payment-product}";
        static final String PAYMENT_REQUEST =
                BASE_URL_WITH_ASPSP_CODE_AND_VERSION + "/payments/{payment-product}/{payment-id}";
        static final String GET_PAYMENT_STATUS_REQUEST =
                BASE_URL_WITH_ASPSP_CODE_AND_VERSION
                        + "/payments/{payment-product}/{payment-id}/status";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        static final String PSU_INVOLVED = "psuInvolved";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String TPP_REDIRECT_PREFERRED = "tppRedirectPreferred";
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
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class FormValues {
        public static final String ALL_ACCOUNTS = "all-accounts";
        public static final String PAYMENT_INITIATION_DEFAULT_NAME = "Payment Initiation";
        public static final String PAYMENT_INITIATION_DEFAULT_CHARGE_BEARER = "SHAR";
        public static final Integer FREQUENCY_PER_DAY = 4;
    }

    public static class Formats {
        public static final String CONSENT_BODY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String RSA = "RSA";
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
        public static final String PAYMENT_PRODUCT = "payment-product";
        public static final String PAYMENT_ID = "payment-id";
    }

    public static class SignatureValues {
        public static final String RSA_SHA256 = "rsa-sha256";
    }

    public static class Storage {
        public static final String PAYMENT_REDIRECT_URI = "paymentRedirectUrl";
        public static final String PAYMENT_UPDATE_PSU_URI = "updatePsuIdentification";
        public static final String STATE = "payment_state";
    }

    public static class SibsSignSteps {
        public static final String SIBS_PAYMENT_POST_SIGN_STATE = "sibs_payment_post_sign_state";
        public static final String SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS =
                "sibs_manual_authentication_in_progress";
    }
}
