package se.tink.agent.sdk.steppable_execution.base_step;

public interface BaseStep<T> {
    StepResponse<T> executeInternal(StepRequest request);
}
