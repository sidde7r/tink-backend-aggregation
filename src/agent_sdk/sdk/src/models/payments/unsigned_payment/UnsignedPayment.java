package se.tink.agent.sdk.models.payments.unsigned_payment;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.agent.sdk.storage.SerializableReference;

public class UnsignedPayment {
    private final SerializableReference bankReference;

    UnsignedPayment(SerializableReference bankReference) {
        this.bankReference = bankReference;
    }

    public String getBankReference() {
        return this.getBankReference(String.class);
    }

    public <T> T getBankReference(Class<T> referenceType) {
        return Optional.ofNullable(this.bankReference)
                .flatMap(reference -> reference.tryGet(referenceType))
                .orElse(null);
    }

    public static UnsignedPayment fromBankReference(String bankReference) {
        Preconditions.checkNotNull(bankReference);
        return new UnsignedPayment(SerializableReference.from(bankReference));
    }

    public static UnsignedPayment fromBankReference(Object bankReference) {
        Preconditions.checkNotNull(bankReference);
        return new UnsignedPayment(SerializableReference.from(bankReference));
    }
}
