package se.tink.agent.sdk.steppable_execution.interactive_step.response;

import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.IntermediateStepResponseBuildStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.UserInteractionBuildStep;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class IntermediateStepResponseBuilder
        implements UserInteractionBuildStep<IntermediateStepResponseBuildStep>,
                IntermediateStepResponseBuildStep {

    private final String nextStepId;

    private UserInteraction<?> userInteraction;

    IntermediateStepResponseBuilder(String nextStepId) {
        this.nextStepId = nextStepId;
    }

    @Override
    public IntermediateStepResponseBuildStep userInteraction(UserInteraction<?> userInteraction) {
        this.userInteraction = userInteraction;
        return this;
    }

    @Override
    public IntermediateStepResponseBuildStep noUserInteraction() {
        this.userInteraction = null;
        return this;
    }

    @Override
    public IntermediateStepResponse build() {
        return new IntermediateStepResponse(this.nextStepId, this.userInteraction);
    }
}
