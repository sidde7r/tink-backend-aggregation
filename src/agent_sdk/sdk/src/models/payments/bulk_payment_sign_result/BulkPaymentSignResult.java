package se.tink.agent.sdk.models.payments.bulk_payment_sign_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder.BulkPaymentSignResultBuildReference;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentSignResult {
    private final PaymentReference reference;
    private final PaymentState state;
    @Nullable private final Debtor debtor;

    BulkPaymentSignResult(PaymentReference reference, PaymentState state, @Nullable Debtor debtor) {
        this.reference = reference;
        this.state = state;
        this.debtor = debtor;
    }

    public PaymentReference getReference() {
        return reference;
    }

    public PaymentState getState() {
        return state;
    }

    public Optional<Debtor> getDebtor() {
        return Optional.ofNullable(debtor);
    }

    public static BulkPaymentSignResultBuildReference builder() {
        return new BulkPaymentSignResultBuilder();
    }
}
