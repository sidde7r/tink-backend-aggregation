package se.tink.agent.sdk.payments.beneficiary.steppable_execution;

import java.util.Map;
import se.tink.agent.sdk.models.payments.BeneficiaryReference;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;

public class BeneficiarySignFlow extends ExecutionFlowImpl<BeneficiaryReference, BeneficiaryState> {
    private BeneficiarySignFlow(
            String startStepId,
            Map<String, BaseStep<BeneficiaryReference, BeneficiaryState>> steps) {
        super(startStepId, steps);
    }

    public static InteractiveFlowStartStep<
                    BeneficiaryReference, BeneficiaryState, BeneficiarySignFlow>
            builder() {
        return new ExecutionFlowBuilder<>(BeneficiarySignFlow::new);
    }
}
