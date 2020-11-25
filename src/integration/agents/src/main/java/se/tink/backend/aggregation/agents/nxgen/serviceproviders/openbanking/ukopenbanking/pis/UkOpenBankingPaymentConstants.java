package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

public class UkOpenBankingPaymentConstants {

    public static class HttpHeaders {
        static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
        static final String X_JWS_SIGNATURE = "x-jws-signature";
    }

    public static class ApiServices {
        public static final String PAYMENTS = "/payments";

        public static class Domestic {
            public static final String PAYMENT_CONSENT = "/domestic-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/domestic-payment-consents/{consentId}";
            public static final String PAYMENT_FUNDS_CONFIRMATION =
                    "/domestic-payment-consents/{consentId}/funds-confirmation";
            public static final String PAYMENT = "/domestic-payments";
            public static final String PAYMENT_STATUS = "/domestic-payments/{paymentId}";
        }

        public static class DomesticScheduled {
            public static final String PAYMENT_CONSENT = "/domestic-scheduled-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/domestic-scheduled-payment-consents/{consentId}";
            public static final String PAYMENT = "/domestic-scheduled-payments";
            public static final String PAYMENT_STATUS = "/domestic-scheduled-payments/{paymentId}";
        }

        public static class UrlParameterKeys {
            public static final String CONSENT_ID = "consentId";
            public static final String PAYMENT_ID = "paymentId";
        }
    }

    public static class JWTSignatureHeaders {
        public static class Headers {
            static final String IAT = "http://openbanking.org.uk/iat";
            static final String ISS = "http://openbanking.org.uk/iss";
            public static final String TAN = "http://openbanking.org.uk/tan";
            static final String B64 = "b64";
            static final String CRIT = "crit";
        }

        public static class Payload {
            public static final String DATA = "Data";
            static final String RISK = "Risk";
        }
    }
}
