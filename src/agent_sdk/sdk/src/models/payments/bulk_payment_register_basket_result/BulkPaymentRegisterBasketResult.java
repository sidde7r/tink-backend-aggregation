package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder.BulkPaymentRegisterBasketResultBuildReference;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.storage.SerializableReference;

public class BulkPaymentRegisterBasketResult {
    @Nullable private final SerializableReference bankBasketReference;

    private final List<BulkPaymentRegisterResult> paymentRegisterResults;

    BulkPaymentRegisterBasketResult(
            @Nullable SerializableReference bankBasketReference,
            List<BulkPaymentRegisterResult> paymentRegisterResults) {
        this.bankBasketReference = bankBasketReference;
        this.paymentRegisterResults = paymentRegisterResults;
    }

    public Optional<SerializableReference> getBankBasketReference() {
        return Optional.ofNullable(bankBasketReference);
    }

    public List<BulkPaymentRegisterResult> getPaymentRegisterResults() {
        return paymentRegisterResults;
    }

    public static BulkPaymentRegisterBasketResultBuildReference builder() {
        return new BulkPaymentRegisterBasketResultBuilder();
    }
}
