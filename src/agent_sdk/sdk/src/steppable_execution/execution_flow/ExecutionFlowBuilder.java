package se.tink.agent.sdk.steppable_execution.execution_flow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowAddStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.InteractiveFlowStartStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.NonInteractiveFlowAddStep;
import se.tink.agent.sdk.steppable_execution.execution_flow.builder.NonInteractiveFlowStartStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public class ExecutionFlowBuilder<T, R, S>
        implements InteractiveFlowStartStep<T, R, S>,
                InteractiveFlowAddStep<T, R, S>,
                NonInteractiveFlowStartStep<T, R, S>,
                NonInteractiveFlowAddStep<T, R, S> {
    private String startStepId;
    private final Map<String, BaseStep<T, R>> steps = new HashMap<>();
    private final BiFunction<String, Map<String, BaseStep<T, R>>, S> builderMethod;

    public ExecutionFlowBuilder(BiFunction<String, Map<String, BaseStep<T, R>>, S> builderMethod) {
        this.builderMethod = builderMethod;
    }

    private void addStepInternal(BaseStep<T, R> step) {
        String stepId = step.getClass().toString();
        if (this.steps.containsKey(stepId)) {
            throw new DuplicateStepException(stepId);
        }
        this.steps.put(stepId, step);
    }

    @Override
    public S build() {
        return this.builderMethod.apply(this.startStepId, this.steps);
    }

    @Override
    public InteractiveFlowAddStep<T, R, S> startStep(InteractiveStep<T, R> step) {
        this.startStepId = step.getClass().toString();
        addStepInternal(step);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InteractiveFlowAddStep<T, R, S> startStep(IntermediateStep step) {
        this.startStepId = step.getClass().toString();
        addStepInternal((BaseStep<T, R>) step);
        return this;
    }

    @Override
    public InteractiveFlowAddStep<T, R, S> addStep(InteractiveStep<T, R> step) {
        addStepInternal(step);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InteractiveFlowAddStep<T, R, S> addStep(IntermediateStep step) {
        addStepInternal((BaseStep<T, R>) step);
        return this;
    }

    @Override
    public NonInteractiveFlowAddStep<T, R, S> startStep(NonInteractiveStep<T, R> step) {
        this.startStepId = step.getClass().toString();
        addStepInternal(step);
        return this;
    }

    @Override
    public NonInteractiveFlowAddStep<T, R, S> addStep(NonInteractiveStep<T, R> step) {
        addStepInternal(step);
        return this;
    }
}
