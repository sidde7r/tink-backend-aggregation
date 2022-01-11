package se.tink.agent.sdk.steppable_execution.execution_flow;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;

public class InteractiveExecutionFlow<T> extends ExecutionFlowImpl<T> {
    InteractiveExecutionFlow(String startStepId, Map<String, BaseStep<T>> steps) {
        super(startStepId, ImmutableMap.copyOf(steps));
    }

    public static <T> InteractiveExecutionFlowBuilder<T> startStep(InteractiveStep<T> step) {
        return new InteractiveExecutionFlowBuilder<>(step);
    }

    public static <T> InteractiveExecutionFlowBuilder<T> startStep(IntermediateStep step) {
        return new InteractiveExecutionFlowBuilder<>(step);
    }
}
