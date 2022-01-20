package se.tink.agent.sdk.payments.bulk.steppable_execution;

import java.util.Map;
import se.tink.agent.sdk.models.payments.BulkPaymentSigningBasket;
import se.tink.agent.sdk.models.payments.bulk_payment_sign_basket_result.BulkPaymentSignBasketResult;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;

public class BulkPaymentSignFlow
        extends ExecutionFlowImpl<BulkPaymentSigningBasket, BulkPaymentSignBasketResult> {

    private BulkPaymentSignFlow(
            String startStepId,
            Map<String, BaseStep<BulkPaymentSigningBasket, BulkPaymentSignBasketResult>> steps) {
        super(startStepId, steps);
    }

    public static InteractiveFlowStartStep<
                    BulkPaymentSigningBasket, BulkPaymentSignBasketResult, BulkPaymentSignFlow>
            builder() {
        return new ExecutionFlowBuilder<>(BulkPaymentSignFlow::new);
    }
}
