package se.tink.agent.sdk.steppable_execution.execution_flow;

import java.util.HashMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;

public class InteractiveExecutionFlowBuilder<T, R> {
    private final String startStepId;
    private final Map<String, BaseStep<T, R>> steps;

    private InteractiveExecutionFlowBuilder(String startStepId) {
        this.startStepId = startStepId;
        this.steps = new HashMap<>();
    }

    InteractiveExecutionFlowBuilder(InteractiveStep<T, R> startStep) {
        this(startStep.getClass().toString());
        addStep(startStep);
    }

    InteractiveExecutionFlowBuilder(IntermediateStep startStep) {
        this(startStep.getClass().toString());
        addStep(startStep);
    }

    private InteractiveExecutionFlowBuilder<T, R> addStepInternal(BaseStep<T, R> step) {
        String stepId = step.getClass().toString();
        if (this.steps.containsKey(stepId)) {
            throw new DuplicateStepException(stepId);
        }
        this.steps.put(stepId, step);
        return this;
    }

    public InteractiveExecutionFlowBuilder<T, R> addStep(InteractiveStep<T, R> step) {
        return addStepInternal(step);
    }

    @SuppressWarnings("unchecked")
    public InteractiveExecutionFlowBuilder<T, R> addStep(IntermediateStep step) {
        return addStepInternal((BaseStep<T, R>) step);
    }

    public InteractiveExecutionFlow<T, R> build() {
        return new InteractiveExecutionFlow<>(this.startStepId, this.steps);
    }
}
