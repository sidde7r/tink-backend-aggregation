package se.tink.agent.sdk.steppable_execution.interactive_step;

import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;

public abstract class IntermediateStep implements BaseStep<Void, Void> {
    @Override
    public final StepResponse<Void> executeInternal(StepRequest<Void> request) {
        return this.execute(request);
    }

    public abstract IntermediateStepResponse execute(StepRequest<Void> request);
}
