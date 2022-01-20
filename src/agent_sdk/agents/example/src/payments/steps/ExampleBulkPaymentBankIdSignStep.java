package se.tink.agent.agents.example.payments.steps;

import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_result.BulkPaymentSignResult;
import se.tink.agent.sdk.payments.bulk.steppable_execution.BulkPaymentSignStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public class ExampleBulkPaymentBankIdSignStep extends BulkPaymentSignStep {
    @Override
    public InteractiveStepResponse<BulkPaymentSignBasketResult> execute(
            StepRequest<BulkPaymentSigningBasket> request) {

        BulkPaymentSigningBasket paymentSigningBasket = request.getStepArgument();

        BulkPaymentSignResult.builder()
                .reference(null)
                .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                .noDebtor()
                .build();

        BulkPaymentSignBasketResult bulkPaymentSignBasketResult =
                BulkPaymentSignBasketResult.builder()
                        .allWithSameError(
                                paymentSigningBasket, PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                        .build();

        return InteractiveStepResponse.done(bulkPaymentSignBasketResult);
    }
}
