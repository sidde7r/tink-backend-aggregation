package se.tink.agent.sdk.steppable_execution.non_interactive_step;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.steppable_execution.base_step.StepResponse;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class NonInteractionStepResponse<T> implements StepResponse<T> {
    @Nullable private final T donePayload;
    @Nullable private final String nextStepId;

    private NonInteractionStepResponse(T donePayload) {
        this.donePayload = Preconditions.checkNotNull(donePayload);
        this.nextStepId = null;
    }

    public NonInteractionStepResponse(String nextStepId) {
        this.nextStepId = Preconditions.checkNotNull(nextStepId);
        this.donePayload = null;
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
        return Optional.empty();
    }

    public static <T> NonInteractionStepResponse<T> nextStep(
            Class<? extends NonInteractiveStep<T>> nextStep) {
        return new NonInteractionStepResponse<>(nextStep.toString());
    }

    public static <T> NonInteractionStepResponse<T> done(T donePayload) {
        return new NonInteractionStepResponse<>(donePayload);
    }
}
