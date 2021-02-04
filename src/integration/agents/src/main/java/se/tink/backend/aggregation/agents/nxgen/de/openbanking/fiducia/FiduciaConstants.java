package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class FiduciaConstants {

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "PNDG", "RCVD")
                    .put(PaymentStatus.CANCELLED, "CANC")
                    .put(PaymentStatus.REJECTED, "RJCT")
                    .put(PaymentStatus.SIGNED, "ACCP")
                    .build();

    private FiduciaConstants() {
        throw new AssertionError();
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "consent-id";
        public static final String STATE = "state";
        public static final String PSU_ID = "psu-id";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "consent-id";
        public static final String DATE = "date";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String SIGNATURE = "signature";
        public static final String DIGEST = "digest";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String PSU_ID = "psu-id";
        public static final String PSU_CORPORATE_ID = "psu-corporate-id";
        public static final String ACCEPT = "accept";
        public static final String TPP_ID = "tpp-id";
        public static final String TPP_REDIRECT_URI = "tpp-redirect";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
    }

    public static class QueryParamsKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
    }

    public static class QueryParamsValues {
        public static final String BOOKING_STATUS = "booked";
        public static final String DATE_FROM = "1970-01-01";
    }

    public static class FormValues {
        public static final String VALID_UNTIL = "9999-12-31";
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String DATE_FORMAT = "dd MM yyyy";
        public static final String OTHER_ID = "123";
        public static final String SCHEME_NAME = "PISP";
        public static final String PAYMENT_INITIATOR = "PaymentInitiator";
        public static final String NUMBER_OF_TRANSACTIONS = "1";
        public static final String MESSAGE_ID = "MIPI-123456789RI-123456789";
        public static final String PAYMENT_ID = "RI-123456789";
        public static final String RMT_INF = "Ref Number Merchant-123456";
        public static final String PAYMENT_TYPE = "SEPA";
        public static final String CHRG_BR = "SLEV";
        public static final String PAYMENT_INFORMATION_ID = "BIPI-123456789RI-123456789";
        public static final String PAYMENT_METHOD = "TRF";
    }

    public static class SignatureKeys {
        public static final String KEY_ID = "keyId=\"";
        public static final String ALGORITHM = ", algorithm=\"";
        public static final String HEADERS = ", headers=\"";
        public static final String SIGNATURE = ", signature=\"";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String DIGEST = "digest";
        public static final String DATE = "date";
        public static final String PSU_ID = "psu-id";
        public static final String EMPTY = "";
        public static final String SHA_256 = "SHA-256=";
    }

    public static class SignatureValues {
        public static final String ALGORITHM = "SHA256withRSA";
        public static final String HEADERS_WITH_PSU_ID =
                "date digest x-request-id psu-id tpp-redirect-uri";
        public static final String HEADERS = "x-request-id digest date";
        public static final String EMPTY_BODY = "";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
        public static final String PSU_ID = "psu-id";
        public static final String PASSWORD = "password";
    }
}
