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

    public static final class StorageValues {
        public static final String SCA_LINKS = "sca-links";
    }

    public static final class ErrorMessages {
        public static final String MISSING_SCA_URL = "Sca Authorization Url missing";
    }
}
