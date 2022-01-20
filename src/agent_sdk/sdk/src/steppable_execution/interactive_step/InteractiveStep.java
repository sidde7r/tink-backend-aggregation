package se.tink.agent.sdk.steppable_execution.interactive_step;

import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public abstract class InteractiveStep<T, R> implements BaseStep<T, R> {
    @Override
    public final StepResponse<R> executeInternal(StepRequest<T> request) {
        return this.execute(request);
    }

    public abstract InteractiveStepResponse<R> execute(StepRequest<T> request);
}
