package se.tink.agent.sdk.steppable_execution.interactive_step.response;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.InteractiveStepResponseBuildStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.builder.UserInteractionBuildStep;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class InteractiveStepResponse<T> implements StepResponse<T> {
    @Nullable private final T donePayload;
    @Nullable private final String nextStepId;
    @Nullable private final UserInteraction<?> userInteraction;

    InteractiveStepResponse(String nextStepId, @Nullable UserInteraction<?> userInteraction) {
        this.nextStepId = Preconditions.checkNotNull(nextStepId);
        this.userInteraction = userInteraction;
        this.donePayload = null;
    }

    InteractiveStepResponse(T donePayload) {
        this.donePayload = Preconditions.checkNotNull(donePayload);
        this.nextStepId = null;
        this.userInteraction = null;
    }

    @Override
    public final Optional<T> getDonePayload() {
        return Optional.ofNullable(this.donePayload);
    }

    @Override
    public final Optional<String> getNextStepId() {
        return Optional.ofNullable(this.nextStepId);
    }

    @Override
    public final Optional<UserInteraction<?>> getUserInteraction() {
        return Optional.ofNullable(this.userInteraction);
    }

    public static UserInteractionBuildStep<InteractiveStepResponseBuildStep> nextStep(
            Class<? extends BaseStep<?, ?>> nextStep) {
        return new InteractiveStepResponseBuilder(nextStep.toString());
    }

    public static <T> InteractiveStepResponse<T> done(T donePayload) {
        return new InteractiveStepResponse<>(donePayload);
    }
}
