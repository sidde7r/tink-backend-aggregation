package se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.builder;

import java.util.List;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;

public interface BulkPaymentSignBasketResultBuildAll {

    BulkPaymentSignBasketResultBuild allWithSameStatus(
            BulkPaymentSigningBasket signingBasket, PaymentStatus status);

    BulkPaymentSignBasketResultBuild allWithSameError(
            BulkPaymentSigningBasket signingBasket, PaymentError error);

    BulkPaymentSignBasketResultBuild paymentResults(List<BulkPaymentSignResult> paymentSignResults);

    BulkPaymentSignBasketResultBuildOne paymentResult(BulkPaymentSignResult paymentSignResult);
}
