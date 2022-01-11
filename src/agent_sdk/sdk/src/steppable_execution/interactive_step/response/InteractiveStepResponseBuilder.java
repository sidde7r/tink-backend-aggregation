package se.tink.agent.sdk.steppable_execution.interactive_step.response;

import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.InteractiveStepResponseBuildStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.UserInteractionBuildStep;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class InteractiveStepResponseBuilder
        implements UserInteractionBuildStep<InteractiveStepResponseBuildStep>,
                InteractiveStepResponseBuildStep {

    private final String nextStepId;
    private UserInteraction<?> userInteraction;

    InteractiveStepResponseBuilder(String nextStepId) {
        this.nextStepId = nextStepId;
    }

    @Override
    public InteractiveStepResponseBuildStep userInteraction(UserInteraction<?> userInteraction) {
        this.userInteraction = userInteraction;
        return this;
    }

    @Override
    public InteractiveStepResponseBuildStep noUserInteraction() {
        this.userInteraction = null;
        return this;
    }

    @Override
    public <T> InteractiveStepResponse<T> build() {
        return new InteractiveStepResponse<>(this.nextStepId, this.userInteraction);
    }
}
