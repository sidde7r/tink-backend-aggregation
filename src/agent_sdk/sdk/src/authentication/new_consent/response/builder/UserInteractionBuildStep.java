package se.tink.agent.sdk.authentication.new_consent.response.builder;

import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface UserInteractionBuildStep {
    BuildStep userInteraction(UserInteraction<?> userInteraction);

    BuildStep noUserInteraction();
}
