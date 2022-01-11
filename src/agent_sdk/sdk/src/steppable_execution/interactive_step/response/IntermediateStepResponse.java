package se.tink.agent.sdk.steppable_execution.interactive_step.response;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.IntermediateStepResponseBuildStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.UserInteractionBuildStep;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class IntermediateStepResponse implements StepResponse<Void> {
    private final String nextStepId;
    @Nullable private final UserInteraction<?> userInteraction;

    IntermediateStepResponse(String nextStepId, @Nullable UserInteraction<?> userInteraction) {
        this.nextStepId = nextStepId;
        this.userInteraction = userInteraction;
    }

    @Override
    public final Optional<Void> getDonePayload() {
        return Optional.empty();
    }

    @Override
    public final Optional<String> getNextStepId() {
        return Optional.of(nextStepId);
    }

    @Override
    public final Optional<UserInteraction<?>> getUserInteraction() {
        return Optional.ofNullable(userInteraction);
    }

    public static UserInteractionBuildStep<IntermediateStepResponseBuildStep> nextStep(
            Class<? extends BaseStep<?>> nextStep) {
        return new IntermediateStepResponseBuilder(nextStep.toString());
    }
}
