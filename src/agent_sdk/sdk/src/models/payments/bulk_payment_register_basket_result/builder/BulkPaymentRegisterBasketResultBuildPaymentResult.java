package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder;

import se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.BulkPaymentRegisterBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;

public interface BulkPaymentRegisterBasketResultBuildPaymentResult {
    BulkPaymentRegisterBasketResultBuildPaymentResult paymentResult(
            BulkPaymentRegisterResult paymentRegisterResult);

    BulkPaymentRegisterBasketResult build();
}
