package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder;

public interface BulkPaymentRegisterBasketResultBuildReference {
    BulkPaymentRegisterBasketResultBuildPaymentResultsAll basketReference(String reference);

    BulkPaymentRegisterBasketResultBuildPaymentResultsAll basketReference(Object reference);

    BulkPaymentRegisterBasketResultBuildPaymentResults noBasketReference();
}
