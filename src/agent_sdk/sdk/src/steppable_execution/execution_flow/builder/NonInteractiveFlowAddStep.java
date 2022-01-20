package se.tink.agent.sdk.steppable_execution.execution_flow.builder;

import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public interface NonInteractiveFlowAddStep<T, R, S> extends ExecutionFlowBuild<S> {
    NonInteractiveFlowAddStep<T, R, S> addStep(NonInteractiveStep<T, R> step);
}
