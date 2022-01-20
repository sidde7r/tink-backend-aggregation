package se.tink.agent.sdk.steppable_execution.execution_flow.builder;

import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public interface NonInteractiveFlowStartStep<T, R, S> {
    NonInteractiveFlowAddStep<T, R, S> startStep(NonInteractiveStep<T, R> step);
}
