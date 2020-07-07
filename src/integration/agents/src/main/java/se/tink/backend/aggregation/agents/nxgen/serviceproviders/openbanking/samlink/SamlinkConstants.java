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
        public static final String TOKEN = "/samlink-api-sandbox/oauth/token";
        public static final String AUTH = "/samlink-api-sandbox/oauth/authorize";

        public static final String AIS_PRODUCT = "/psd2/v1";
        public static final String CONSENT = AIS_PRODUCT + "/consents";
        public static final String ACCOUNTS = AIS_PRODUCT + "/accounts";
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
        public static final String SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    }

    public static class IdTags {
        public static final String PAYMENT_ID = "paymentId";
    }
}
