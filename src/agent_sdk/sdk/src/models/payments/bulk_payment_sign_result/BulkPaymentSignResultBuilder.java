package se.tink.agent.sdk.models.payments.bulk_payment_sign_result;

import com.google.common.base.Preconditions;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuild;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuildDebtor;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuildReference;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuildStatus;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentSignResultBuilder
        implements BulkPaymentSignResultBuildReference,
                BulkPaymentSignResultBuildStatus,
                BulkPaymentSignResultBuildDebtor,
                BulkPaymentSignResultBuild {

    private PaymentReference paymentReference;
    private PaymentState paymentState;
    private Debtor debtor;

    BulkPaymentSignResultBuilder() {}

    @Override
    public BulkPaymentSignResultBuildStatus reference(PaymentReference paymentReference) {
        this.paymentReference = Preconditions.checkNotNull(paymentReference);
        return this;
    }

    @Override
    public BulkPaymentSignResultBuildDebtor status(PaymentStatus status) {
        Preconditions.checkNotNull(status);
        this.paymentState = PaymentState.create(status);
        return this;
    }

    @Override
    public BulkPaymentSignResultBuildDebtor error(PaymentError error) {
        Preconditions.checkNotNull(error);
        this.paymentState = PaymentState.create(error);
        return this;
    }

    @Override
    public BulkPaymentSignResultBuild debtor(Debtor debtor) {
        this.debtor = Preconditions.checkNotNull(debtor);
        return this;
    }

    @Override
    public BulkPaymentSignResultBuild noDebtor() {
        this.debtor = null;
        return this;
    }

    @Override
    public BulkPaymentSignResult build() {
        return new BulkPaymentSignResult(this.paymentReference, this.paymentState, this.debtor);
    }
}
