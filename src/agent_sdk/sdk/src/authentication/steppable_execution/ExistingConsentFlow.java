package se.tink.agent.sdk.authentication.steppable_execution;

import java.util.Map;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.NonInteractiveFlowStartStep;

public class ExistingConsentFlow extends ExecutionFlowImpl<Void, ConsentStatus> {
    private ExistingConsentFlow(
            String startStepId, Map<String, BaseStep<Void, ConsentStatus>> steps) {
        super(startStepId, steps);
    }

    public static NonInteractiveFlowStartStep<Void, ConsentStatus, ExistingConsentFlow> builder() {
        return new ExecutionFlowBuilder<>(ExistingConsentFlow::new);
    }
}
