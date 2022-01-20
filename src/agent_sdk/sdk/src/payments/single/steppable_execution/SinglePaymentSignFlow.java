package se.tink.agent.sdk.payments.single.steppable_execution;

import java.util.Map;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.SinglePaymentSignResult;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;

public class SinglePaymentSignFlow
        extends ExecutionFlowImpl<PaymentReference, SinglePaymentSignResult> {
    private SinglePaymentSignFlow(
            String startStepId,
            Map<String, BaseStep<PaymentReference, SinglePaymentSignResult>> steps) {
        super(startStepId, steps);
    }

    public static InteractiveFlowStartStep<
                    PaymentReference, SinglePaymentSignResult, SinglePaymentSignFlow>
            builder() {
        return new ExecutionFlowBuilder<>(SinglePaymentSignFlow::new);
    }
}
