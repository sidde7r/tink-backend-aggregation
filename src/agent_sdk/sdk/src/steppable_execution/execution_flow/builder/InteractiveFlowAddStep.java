package se.tink.agent.sdk.steppable_execution.execution_flow.builder;

import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;

public interface InteractiveFlowAddStep<T, R, S> extends ExecutionFlowBuild<S> {
    InteractiveFlowAddStep<T, R, S> addStep(InteractiveStep<T, R> step);

    InteractiveFlowAddStep<T, R, S> addStep(IntermediateStep step);
}
