package se.tink.agent.sdk.steppable_execution.execution_flow;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public class NonInteractiveExecutionFlow<T> extends ExecutionFlowImpl<T> {
    NonInteractiveExecutionFlow(String startStepId, Map<String, NonInteractiveStep<T>> steps) {
        super(startStepId, ImmutableMap.copyOf(steps));
    }

    public static <T> NonInteractiveExecutionFlowBuilder<T> startStep(NonInteractiveStep<T> step) {
        return new NonInteractiveExecutionFlowBuilder<>(step);
    }
}
