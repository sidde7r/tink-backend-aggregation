package se.tink.agent.sdk.steppable_execution.non_interactive_step;

import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequestBase;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;

public abstract class NonInteractiveStep<T> implements BaseStep<T> {
    @Override
    public final StepResponse<T> executeInternal(StepRequest request) {
        return this.execute(request);
    }

    public abstract NonInteractionStepResponse<T> execute(StepRequestBase request);
}
