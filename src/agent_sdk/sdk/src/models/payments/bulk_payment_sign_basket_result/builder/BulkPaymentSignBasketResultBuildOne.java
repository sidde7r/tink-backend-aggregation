package se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.builder;

import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;

public interface BulkPaymentSignBasketResultBuildOne {
    BulkPaymentSignBasketResultBuildOne paymentResult(BulkPaymentSignResult paymentSignResult);

    BulkPaymentSignBasketResult build();
}
