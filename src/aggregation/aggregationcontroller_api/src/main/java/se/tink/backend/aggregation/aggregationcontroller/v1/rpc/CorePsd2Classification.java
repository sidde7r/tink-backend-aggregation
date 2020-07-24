package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;

public enum CorePsd2Classification {
    UNDETERMINED,
    PAYMENT,
    NON_PAYMENT;

    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(CorePsd2Classification.class);

    public static CorePsd2Classification of(Psd2PaymentAccountClassificationResult classification) {
        switch (classification) {
            case PAYMENT_ACCOUNT:
                return PAYMENT;
            case NON_PAYMENT_ACCOUNT:
                return NON_PAYMENT;
            case UNDETERMINED_PAYMENT_ACCOUNT:
                return UNDETERMINED;
            default:
                log.warn("Could not map {}", classification);
                throw new IllegalArgumentException("No mapping available for: " + classification);
        }
    }
}
