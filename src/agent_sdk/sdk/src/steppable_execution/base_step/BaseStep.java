package se.tink.agent.sdk.steppable_execution.base_step;

public abstract class BaseStep<T> {
    public abstract StepResponse<T> executeInternal(StepRequest request);
}
