package se.tink.agent.sdk.models.payments.payment_reference;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.builder.PaymentReferenceBuildPayment;
import se.tink.agent.sdk.storage.SerializableReference;

/**
 * A data structure that bridge Tink's version of a Payment and the bank. The `bankReference` is
 * optional.
 */
@EqualsAndHashCode
public class PaymentReference {
    private final Payment payment;
    @Nullable private final SerializableReference bankReference;

    PaymentReference(Payment payment, @Nullable SerializableReference bankReference) {
        this.payment = payment;
        this.bankReference = bankReference;
    }

    public Payment getPayment() {
        return this.payment;
    }

    @Nullable
    public String getBankReference() {
        return this.getBankReference(String.class);
    }

    @Nullable
    public <T> T getBankReference(Class<T> referenceType) {
        return Optional.ofNullable(this.bankReference)
                .flatMap(reference -> reference.tryGet(referenceType))
                .orElse(null);
    }

    public static PaymentReferenceBuildPayment builder() {
        return new PaymentReferenceBuilder();
    }
}
