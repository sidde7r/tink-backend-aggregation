package se.tink.agent.sdk.authentication.steppable_execution;

import java.util.Map;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowBuilder;
import se.tink.agent.sdk.steppable_execution.execution_flow.ExecutionFlowImpl;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;

public class NewConsentFlow extends ExecutionFlowImpl<Void, ConsentLifetime> {
    private NewConsentFlow(String startStepId, Map<String, BaseStep<Void, ConsentLifetime>> steps) {
        super(startStepId, steps);
    }

    public static InteractiveFlowStartStep<Void, ConsentLifetime, NewConsentFlow> builder() {
        return new ExecutionFlowBuilder<>(NewConsentFlow::new);
    }
}
