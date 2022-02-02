package se.tink.agent.sdk.models.payments;

import java.util.List;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public interface BulkPaymentSigningBasket {
    @Nullable
    default String getBankBasketReference() {
        return this.getBankBasketReference(String.class);
    }

    @Nullable
    <T> T getBankBasketReference(Class<T> referenceType);

    List<PaymentReference> getPaymentReferences();
}
