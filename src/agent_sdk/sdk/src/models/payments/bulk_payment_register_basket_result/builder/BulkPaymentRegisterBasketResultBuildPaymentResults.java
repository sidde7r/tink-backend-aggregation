package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder;

import java.util.List;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;

public interface BulkPaymentRegisterBasketResultBuildPaymentResults {
    BulkPaymentRegisterBasketResultBuild paymentResults(
            List<BulkPaymentRegisterResult> paymentRegisterResults);

    BulkPaymentRegisterBasketResultBuildPaymentResult paymentResult(
            BulkPaymentRegisterResult paymentRegisterResult);
}
