package se.tink.agent.sdk.steppable_execution.base_step;

public interface BaseStep<T, R> {
    StepResponse<R> executeInternal(StepRequest<T> request);
}
