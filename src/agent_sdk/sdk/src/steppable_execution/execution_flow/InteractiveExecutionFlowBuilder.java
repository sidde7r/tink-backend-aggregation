package se.tink.agent.sdk.steppable_execution.execution_flow;

import java.util.HashMap;
import java.util.Map;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;

public class InteractiveExecutionFlowBuilder<T> {
    private final String startStepId;
    private final Map<String, BaseStep<T>> steps;

    private InteractiveExecutionFlowBuilder(String startStepId) {
        this.startStepId = startStepId;
        this.steps = new HashMap<>();
    }

    InteractiveExecutionFlowBuilder(InteractiveStep<T> startStep) {
        this(startStep.getClass().toString());
        addStep(startStep);
    }

    InteractiveExecutionFlowBuilder(IntermediateStep startStep) {
        this(startStep.getClass().toString());
        addStep(startStep);
    }

    private InteractiveExecutionFlowBuilder<T> addStepInternal(BaseStep<T> step) {
        String stepId = step.getClass().toString();
        if (this.steps.containsKey(stepId)) {
            throw new DuplicateStepException(stepId);
        }
        this.steps.put(stepId, step);
        return this;
    }

    public InteractiveExecutionFlowBuilder<T> addStep(InteractiveStep<T> step) {
        return addStepInternal(step);
    }

    @SuppressWarnings("unchecked")
    public InteractiveExecutionFlowBuilder<T> addStep(IntermediateStep step) {
        return addStepInternal((BaseStep<T>) step);
    }

    public InteractiveExecutionFlow<T> build() {
        return new InteractiveExecutionFlow<>(this.startStepId, this.steps);
    }
}
