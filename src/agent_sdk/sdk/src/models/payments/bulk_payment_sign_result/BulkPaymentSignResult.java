package se.tink.agent.sdk.models.payments.bulk_payment_sign_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuildReference;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentSignResult {
    private final PaymentReference paymentReference;
    private final PaymentState paymentState;

    // TODO: Should this be a smaller scope? E.g. only AccountIdentifier?
    @Nullable private final Debtor paymentDebtor;

    BulkPaymentSignResult(
            PaymentReference paymentReference,
            PaymentState paymentState,
            @Nullable Debtor paymentDebtor) {
        this.paymentReference = paymentReference;
        this.paymentState = paymentState;
        this.paymentDebtor = paymentDebtor;
    }

    public PaymentReference getPaymentReference() {
        return paymentReference;
    }

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public Optional<Debtor> getPaymentDebtor() {
        return Optional.ofNullable(paymentDebtor);
    }

    public static BulkPaymentSignResultBuildReference builder() {
        return new BulkPaymentSignResultBuilder();
    }
}
