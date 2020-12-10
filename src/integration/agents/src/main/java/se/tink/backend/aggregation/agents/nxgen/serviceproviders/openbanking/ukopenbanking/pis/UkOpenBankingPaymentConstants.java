package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

public class UkOpenBankingPaymentConstants {

    public static class JWTSignatureHeaders {
        public static class Headers {
            static final String IAT = "http://openbanking.org.uk/iat";
            static final String ISS = "http://openbanking.org.uk/iss";
            public static final String TAN = "http://openbanking.org.uk/tan";
            static final String B64 = "b64";
            static final String CRIT = "crit";
        }
    }

    public static final String CONSENT_ID_KEY = "consentId";
    public static final String PAYMENT_ID_KEY = "paymentId";
}
