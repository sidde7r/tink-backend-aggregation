package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class ErstebankConstants {

    public static final String INTEGRATION_NAME = "erstebank-at";

    private ErstebankConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD", "ACCP").build();

    public static class Urls {
        public static final String BASE_AUTH = "/sandbox-idp";
        public static final String AUTH = BASE_AUTH + "/auth";
        public static final String TOKEN = BASE_AUTH + "/token";
        public static final String ACCOUNTS = "/psd2-accounts-api/accounts";
        public static final String CONSENT = "/psd2-consent-api/consents";
        public static final String SIGN_CONSENT = "/psd2-consent-api/consents/%s/authorisations";
        public static final String TRANSACTIONS = "/psd2-accounts-api/accounts/%s/transactions";
        public static final String PAYMENTS = "/psd2-payments-api/payments";
        public static final String CREATE_SEPA = PAYMENTS + "/sepa-credit-transfers";
        public static final String CREATE_CROSS_BORDER =
                PAYMENTS + "/cross-border-credit-transfers";
        public static final String FETCH_SEPA = CREATE_SEPA + "/{paymentId}";
        public static final String FETCH_CROSS_BORDER = CREATE_CROSS_BORDER + "/{paymentId}";
    }

    public static class IdTags {
        public static final String PAYMENT_ID = "paymentId";
    }
}
