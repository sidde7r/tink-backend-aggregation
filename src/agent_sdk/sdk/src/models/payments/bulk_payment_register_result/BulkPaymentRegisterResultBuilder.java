package se.tink.agent.sdk.models.payments.bulk_payment_register_result;

import com.google.common.base.Preconditions;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuild;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuildReference;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuildStatus;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentRegisterResultBuilder
        implements BulkPaymentRegisterResultBuildReference,
                BulkPaymentRegisterResultBuildStatus,
                BulkPaymentRegisterResultBuild {

    private PaymentReference paymentReference;
    private PaymentState paymentState;

    BulkPaymentRegisterResultBuilder() {}

    @Override
    public BulkPaymentRegisterResultBuildStatus reference(PaymentReference paymentReference) {
        this.paymentReference = Preconditions.checkNotNull(paymentReference);
        return this;
    }

    @Override
    public BulkPaymentRegisterResultBuild noError() {
        this.paymentState = PaymentState.create(PaymentStatus.CREATED);
        return this;
    }

    @Override
    public BulkPaymentRegisterResultBuild error(PaymentError error) {
        Preconditions.checkNotNull(error);
        this.paymentState = PaymentState.create(error);
        return this;
    }

    @Override
    public BulkPaymentRegisterResult build() {
        return new BulkPaymentRegisterResult(this.paymentReference, this.paymentState);
    }
}
