package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums;

import se.tink.libraries.payments.common.model.PaymentScheme;

public enum OpBankPaymentOrder {
    SEPA_CREDIT_TRANSFER(PaymentScheme.SEPA_CREDIT_TRANSFER, "SEPA"),
    SEPA_INSTANT_CREDIT_TRANSFER(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER, "SEPA_INST");

    private final PaymentScheme paymentScheme;
    private final String paymentOrder;

    OpBankPaymentOrder(PaymentScheme paymentScheme, String paymentOrder) {
        this.paymentScheme = paymentScheme;
        this.paymentOrder = paymentOrder;
    }

    public static OpBankPaymentOrder orderFromScheme(PaymentScheme givenPaymentScheme) {
        if (givenPaymentScheme == PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER) {
            return SEPA_INSTANT_CREDIT_TRANSFER;
        }
        return SEPA_CREDIT_TRANSFER;
    }

    public String getPaymentOrder() {
        return this.paymentOrder;
    }

    @Override
    public String toString() {
        return paymentOrder;
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }
}
