package se.tink.agent.agents.example.payments.steps;

import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.SinglePaymentSignResult;
import se.tink.agent.sdk.payments.single.steppable_execution.SinglePaymentSignStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public class ExampleSinglePaymentBankIdSignStep extends SinglePaymentSignStep {
    @Override
    public InteractiveStepResponse<SinglePaymentSignResult> execute(
            StepRequest<PaymentReference> request) {
        SinglePaymentSignResult result =
                SinglePaymentSignResult.builder()
                        .error(PaymentError.AMOUNT_LARGER_THAN_BANK_LIMIT)
                        .noDebtor()
                        .build();

        return InteractiveStepResponse.done(result);
    }
}
