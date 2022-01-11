package se.tink.agent.sdk.steppable_execution.execution_flow;

import java.util.HashMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public class NonInteractiveExecutionFlowBuilder<T> {
    private final String startStepId;
    private final Map<String, NonInteractiveStep<T>> steps;

    NonInteractiveExecutionFlowBuilder(NonInteractiveStep<T> startStep) {
        this.startStepId = startStep.getClass().toString();
        this.steps = new HashMap<>();
        addStep(startStep);
    }

    public NonInteractiveExecutionFlowBuilder<T> addStep(NonInteractiveStep<T> step) {
        String stepId = step.getClass().toString();
        if (this.steps.containsKey(stepId)) {
            throw new DuplicateStepException(stepId);
        }
        this.steps.put(stepId, step);
        return this;
    }

    public NonInteractiveExecutionFlow<T> build() {
        return new NonInteractiveExecutionFlow<>(this.startStepId, this.steps);
    }
}
