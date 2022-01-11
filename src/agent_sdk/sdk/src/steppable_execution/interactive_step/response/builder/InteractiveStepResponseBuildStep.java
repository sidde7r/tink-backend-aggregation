package se.tink.agent.sdk.steppable_execution.interactive_step.response.builder;

import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public interface InteractiveStepResponseBuildStep {
    <T> InteractiveStepResponse<T> build();
}
