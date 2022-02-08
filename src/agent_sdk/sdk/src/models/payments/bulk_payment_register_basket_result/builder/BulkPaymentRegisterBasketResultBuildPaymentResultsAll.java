package se.tink.agent.sdk.models.payments.bulk_payment_register_basket_result.builder;

import java.util.List;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_register_result.BulkPaymentRegisterResult;
import se.tink.agent.sdk.models.payments.payment.Payment;

public interface BulkPaymentRegisterBasketResultBuildPaymentResultsAll {

    /**
     * Declare that all payments were successfully registered. Note: the payments will not have
     * individual bank references.
     *
     * @param payments The list of payments that were registered.
     * @return Next build step.
     */
    BulkPaymentRegisterBasketResultBuild allSuccessful(List<Payment> payments);

    /**
     * Declare that all payments failed to be registered with the same error. Use this if there is
     * no granular error per payment registration.
     *
     * @param payments The list of payments that were attempted to be registered.
     * @param error The error for all payments.
     * @return Next build step.
     */
    BulkPaymentRegisterBasketResultBuild allWithSameError(
            List<Payment> payments, PaymentError error);

    /**
     * Declare all payment register results at once.
     *
     * @param paymentRegisterResults A list of payment register results.
     * @return Next build step.
     */
    BulkPaymentRegisterBasketResultBuild paymentResults(
            List<BulkPaymentRegisterResult> paymentRegisterResults);

    /**
     * Declare one payment register result at a time.
     *
     * @param paymentRegisterResult One payment register result.
     * @return Next build step.
     */
    BulkPaymentRegisterBasketResultBuildPaymentResult paymentResult(
            BulkPaymentRegisterResult paymentRegisterResult);
}
