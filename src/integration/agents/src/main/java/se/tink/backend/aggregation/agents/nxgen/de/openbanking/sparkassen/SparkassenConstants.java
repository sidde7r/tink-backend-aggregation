package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class SparkassenConstants {

    private SparkassenConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String MISSING_SCA_AUTHORIZATION_URL = "Sca Authorization Url missing";
        public static final String MISSING_SCA_METHOD_DETAILS = "Sca method details missing";
        public static final String COULD_NOT_PARSE_TRANSACTIONS =
                "Could not parse transactions description";
        public static final String COULD_NOT_INITIALIZE_JAXBCONTEXT =
                "Could not initialize JAXBContext";
        public static final String STARTCODE_NOT_FOUND = "Startcode fo Chip tan not found";

        static final String PSU_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";
        static final String FORMAT_ERROR = "FORMAT_ERROR";
        static final String PSU_ID_TOO_LONG = "PSU-ID zu lang";
        static final String OTP_FORMAT_ERROR =
                "scaAuthenticationData muss auf Ausdruck \"[0-9]{6}\" passen";
        static final String CHALLENGE_FORMAT_INVALID =
                "Format of certain request fields are not matching the XS2A requirements.";
        static final String TEMPORARILY_BLOCKED_ACCOUNT =
                "Ihr Zugang ist vorläufig gesperrt - Bitte PIN-Sperre aufheben";
        static final String BLOCKED_ACCOUNT =
                "Ihr Zugang ist gesperrt - Bitte informieren Sie Ihren Berater";
        public static final String MISSING_LINKS_ENTITY = "Response missing links entity";
        static final String NO_ACTIVE_TAN_MEDIUM = "Kein aktives TAN-Medium gefunden.";
        static final String PLEASE_CHANGE_PIN = "Bitte führen Sie eine PIN-Änderung durch.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://xs2a.f-i-apim.de:8443/fixs2aop-env";
        static final URL CONSENT = new URL(BASE_URL + "/xs2a-api/{bankCode}/v1/consents");
        static final URL UPDATE_SCA_METHOD =
                new URL(CONSENT + "/{consentId}/authorisations/{authorizationId}");
        static final URL FINALIZE_AUTHORIZATION =
                new URL(CONSENT + "/{consentId}/authorisations/{authorizationId}");
        static final URL CONSENT_STATUS = new URL(CONSENT + "/{consentId}/status");
        static final URL CONSENT_DETAILS = new URL(CONSENT + "/{consentId}");
        static final URL FETCH_ACCOUNTS = new URL(BASE_URL + "/xs2a-api/{bankCode}/v1/accounts");
        static final URL FETCH_BALANCES = new URL(FETCH_ACCOUNTS + "/{accountId}/balances");
        static final URL FETCH_TRANSACTIONS = new URL(FETCH_ACCOUNTS + "/{accountId}/transactions");

        public static final URL PAYMENT_INITIATION =
                new URL(
                        SparkassenConstants.Urls.BASE_URL
                                + "/xs2a-api/{bankCode}/v1/{payment-service}/{payment-product}");
        public static final URL FETCH_PAYMENT_STATUS =
                new URL(
                        SparkassenConstants.Urls.BASE_URL
                                + "/xs2a-api/{bankCode}/v1/{payment-service}/{payment-product}/{paymentId}/status");
    }

    static class PathVariables {
        static final String CONSENT_ID = "consentId";
        static final String AUTHORIZATION_ID = "authorizationId";
        static final String ACCOUNT_ID = "accountId";
        static final String BANK_CODE = "bankCode";
    }

    static class QueryKeys {
        static final String DATE_FROM = "dateFrom";
        static final String BOOKING_STATUS = "bookingStatus";
    }

    static class QueryValues {
        static final String BOTH = "both";
    }

    static class HeaderKeys {
        static final String X_REQUEST_ID = "X-Request-ID";
        static final String CONSENT_ID = "Consent-ID";
        static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        static final String PSU_ID = "PSU-ID";
        static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    static class FormValues {
        static final int FREQUENCY_PER_DAY = 4;
    }

    public static class AuthMethods {
        private AuthMethods() {}

        public static final ImmutableList<String> UNSUPPORTED_AUTH_TYPES =
                ImmutableList.of("OPTICAL", "QR");
    }
}
