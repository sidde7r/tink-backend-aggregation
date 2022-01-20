package se.tink.agent.sdk.steppable_execution.interactive_step.response.builder;

import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface UserInteractionBuildStep<T> {
    T userInteraction(UserInteraction<?> userInteraction);

    T noUserInteraction();
}
