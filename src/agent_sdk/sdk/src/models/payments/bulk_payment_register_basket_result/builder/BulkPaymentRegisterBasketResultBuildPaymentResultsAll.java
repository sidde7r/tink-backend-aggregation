package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder;

import java.util.List;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.payment.Payment;

public interface BulkPaymentRegisterBasketResultBuildPaymentResultsAll {

    BulkPaymentRegisterBasketResultBuild allWithSameError(
            List<Payment> payments, PaymentError error);

    BulkPaymentRegisterBasketResultBuild paymentResults(
            List<BulkPaymentRegisterResult> paymentRegisterResults);

    BulkPaymentRegisterBasketResultBuildPaymentResult paymentResult(
            BulkPaymentRegisterResult paymentRegisterResult);
}
