package se.tink.agent.sdk.steppable_execution.execution_flow;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public class NonInteractiveExecutionFlow<T, R> extends ExecutionFlowImpl<T, R> {
    NonInteractiveExecutionFlow(String startStepId, Map<String, NonInteractiveStep<T, R>> steps) {
        super(startStepId, ImmutableMap.copyOf(steps));
    }

    public static <T, R> NonInteractiveExecutionFlowBuilder<T, R> startStep(
            NonInteractiveStep<T, R> step) {
        return new NonInteractiveExecutionFlowBuilder<>(step);
    }
}
