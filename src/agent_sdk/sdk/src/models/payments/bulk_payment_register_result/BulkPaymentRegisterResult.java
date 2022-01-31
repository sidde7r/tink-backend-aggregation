package se.tink.agent.sdk.models.payments.bulk_payment_register_result;

import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuildReference;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentRegisterResult {
    private final PaymentReference paymentReference;
    private final PaymentState paymentState;

    BulkPaymentRegisterResult(PaymentReference paymentReference, PaymentState paymentState) {
        this.paymentReference = paymentReference;
        this.paymentState = paymentState;
    }

    public PaymentReference getPaymentReference() {
        return paymentReference;
    }

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public static BulkPaymentRegisterResultBuildReference builder() {
        return new BulkPaymentRegisterResultBuilder();
    }
}
