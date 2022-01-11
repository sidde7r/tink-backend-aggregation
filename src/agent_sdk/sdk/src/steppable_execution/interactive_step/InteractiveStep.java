package se.tink.agent.sdk.steppable_execution.interactive_step;

import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public abstract class InteractiveStep<T> extends BaseStep<T> {
    @Override
    public final StepResponse<T> executeInternal(StepRequest request) {
        return this.execute(request);
    }

    public abstract InteractiveStepResponse<T> execute(StepRequest request);
}
