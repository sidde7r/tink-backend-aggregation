package se.tink.agent.sdk.models.payments.bulk_payment_register_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.ConnectivityError;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder.BulkPaymentRegisterResultBuildReference;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public class BulkPaymentRegisterResult {
    private final PaymentReference reference;
    @Nullable private final PaymentError error;

    BulkPaymentRegisterResult(PaymentReference reference, @Nullable PaymentError error) {
        this.reference = reference;
        this.error = error;
    }

    public PaymentReference getReference() {
        return reference;
    }

    public Optional<ConnectivityError> getError() {
        return Optional.ofNullable(error);
    }

    public static BulkPaymentRegisterResultBuildReference builder() {
        return new BulkPaymentRegisterResultBuilder();
    }
}
