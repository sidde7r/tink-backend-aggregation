package se.tink.agent.sdk.steppable_execution.execution_flow;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;

public class InteractiveExecutionFlow<T, R> extends ExecutionFlowImpl<T, R> {
    InteractiveExecutionFlow(String startStepId, Map<String, BaseStep<T, R>> steps) {
        super(startStepId, ImmutableMap.copyOf(steps));
    }

    public static <T, R> InteractiveExecutionFlowBuilder<T, R> startStep(
            InteractiveStep<T, R> step) {
        return new InteractiveExecutionFlowBuilder<>(step);
    }

    public static <T, R> InteractiveExecutionFlowBuilder<T, R> startStep(IntermediateStep step) {
        return new InteractiveExecutionFlowBuilder<>(step);
    }
}
