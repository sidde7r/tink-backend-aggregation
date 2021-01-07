package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary;

import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

public enum SibsPaymentType {
    SEPA_CREDIT_TRANSFERS("sepa-credit-transfers"),
    CROSS_BORDER_CREDIT_TRANSFERS("cross-border-credit-transfers"),
    INSTANT_SEPA_CREDIT_TRANSFERS("instant-sepa-credit-transfers"),
    TARGET_2_PAYMENTS("target-2-payments");

    private final String value;

    SibsPaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SibsPaymentType fromDomainPayment(Payment payment) {
        if (payment.getPaymentScheme() == PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER) {
            return SibsPaymentType.INSTANT_SEPA_CREDIT_TRANSFERS;
        } else if (payment.isSepa()) {
            return SibsPaymentType.SEPA_CREDIT_TRANSFERS;
        } else {
            return SibsPaymentType.CROSS_BORDER_CREDIT_TRANSFERS;
        }
    }
}
