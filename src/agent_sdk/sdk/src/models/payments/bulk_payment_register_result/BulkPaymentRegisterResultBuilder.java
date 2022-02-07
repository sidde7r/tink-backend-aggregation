package se.tink.agent.sdk.models.payments.bulk_payment_register_result;

import com.google.common.base.Preconditions;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuild;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuildReference;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuildStatus;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentRegisterResultBuilder
        implements BulkPaymentRegisterResultBuildReference,
                BulkPaymentRegisterResultBuildStatus,
                BulkPaymentRegisterResultBuild {

    private PaymentReference reference;
    private PaymentError error;

    BulkPaymentRegisterResultBuilder() {}

    @Override
    public BulkPaymentRegisterResultBuildStatus reference(PaymentReference paymentReference) {
        this.reference = Preconditions.checkNotNull(paymentReference);
        return this;
    }

    @Override
    public BulkPaymentRegisterResultBuild noError() {
        this.error = null;
        return this;
    }

    @Override
    public BulkPaymentRegisterResultBuild error(PaymentError paymentError) {
        this.error = Preconditions.checkNotNull(paymentError);
        return this;
    }

    @Override
    public BulkPaymentRegisterResult build() {
        return new BulkPaymentRegisterResult(this.reference, this.error);
    }
}
