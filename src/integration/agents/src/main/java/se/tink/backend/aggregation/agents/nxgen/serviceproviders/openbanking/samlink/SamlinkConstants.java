package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class SamlinkConstants {

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD").build();

    private SamlinkConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        private Urls() {}

        public static final String TOKEN = "/oauthproxy/token";
        public static final String AUTH = "/oauthproxy/authorize";

        public static final String AIS_PRODUCT = "/psd2/v1";
        public static final String CONSENT = AIS_PRODUCT + "/consents";
        public static final String ACCOUNTS = AIS_PRODUCT + "/accounts";
        public static final String TRANSACTIONS = ACCOUNTS + "/%s/transactions";
        public static final String BALANCES = ACCOUNTS + "/%s/balances";
        public static final String CREATE_SEPA_PAYMENT =
                AIS_PRODUCT + "/payments/sepa-credit-transfers";
        public static final String CREATE_FOREIGN_PAYMENT =
                AIS_PRODUCT + "/payments/cross-border-credit-transfers";
        public static final String GET_SEPA_PAYMENT =
                AIS_PRODUCT + "/payments/sepa-credit-transfers/{paymentId}";
        public static final String GET_FOREIGN_PAYMENT =
                AIS_PRODUCT + "/payments/cross-border-credit-transfers/{paymentId}";
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String API_KEY = "apikey";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    }

    public static class IdTags {
        private IdTags() {}

        public static final String PAYMENT_ID = "paymentId";
    }

    public static final class BookingStatus {
        private BookingStatus() {}

        public static final String BOOKED = "booked";
        public static final String PENDING = "pending";
    }

    public static final class FormKeys {
        private FormKeys() {}

        public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
        public static final String CLIENT_ASSERTION = "client_assertion";
    }

    public static final class FormValues {
        private FormValues() {}

        public static final String CLIENT_ASSERTION_TYPE_VALUE =
                "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    }
}
