package se.tink.agent.sdk.steppable_execution.execution_flow.builder;

import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;

public interface InteractiveFlowStartStep<T, R, S> {
    InteractiveFlowAddStep<T, R, S> startStep(InteractiveStep<T, R> step);

    InteractiveFlowAddStep<T, R, S> startStep(IntermediateStep step);
}
