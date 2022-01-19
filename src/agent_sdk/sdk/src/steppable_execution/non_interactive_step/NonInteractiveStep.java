package se.tink.agent.sdk.steppable_execution.non_interactive_step;

import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequestBase;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;

public abstract class NonInteractiveStep<T, R> implements BaseStep<T, R> {
    @Override
    public final StepResponse<R> executeInternal(StepRequest<T> request) {
        return this.execute(request);
    }

    public abstract NonInteractionStepResponse<R> execute(StepRequestBase<T> request);
}
