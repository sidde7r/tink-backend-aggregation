package se.tink.agent.sdk.steppable_execution.base_step;

import java.util.Optional;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface StepResponse<T> {
    Optional<T> getDonePayload();

    Optional<String> getNextStepId();

    Optional<UserInteraction<?>> getUserInteraction();
}
