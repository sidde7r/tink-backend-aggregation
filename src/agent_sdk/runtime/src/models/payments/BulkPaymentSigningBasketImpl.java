package se.tink.agent.runtime.models.payments;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.storage.Reference;

@AllArgsConstructor
public class BulkPaymentSigningBasketImpl implements BulkPaymentSigningBasket {
    @Nullable private final Reference bankBasketReference;
    private final List<PaymentReference> paymentReferences;

    @Nullable
    @Override
    public <T> T getBankBasketReference(Class<T> referenceType) {
        return Optional.ofNullable(this.bankBasketReference)
                .flatMap(x -> x.tryGet(referenceType))
                .orElse(null);
    }

    @Override
    public List<PaymentReference> getPaymentReferences() {
        return this.paymentReferences;
    }
}
