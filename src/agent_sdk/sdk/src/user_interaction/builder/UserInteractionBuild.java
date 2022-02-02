package se.tink.agent.sdk.user_interaction.builder;

import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface UserInteractionBuild<T> {
    UserInteraction<T> build();
}
