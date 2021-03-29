package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class PaymentConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PathVariables {
        public static final String PAYMENT_PRODUCT = "payment-product";
        public static final String PAYMENT_SERVICE = "payment-service";
        public static final String PAYMENT_ID = "paymentId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HeaderKeys {
        public static final String TPP_REJECTION_NOFUNDS_PREFERRED =
                "TPP-Rejection-NoFunds-Preferred";
    }
}
