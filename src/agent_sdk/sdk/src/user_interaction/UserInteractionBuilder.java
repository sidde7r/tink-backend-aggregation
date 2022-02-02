package se.tink.agent.sdk.user_interaction;

import se.tink.agent.sdk.user_interaction.builder.UserInteractionBuild;
import se.tink.agent.sdk.user_interaction.builder.UserInteractionBuildResponseRequired;

public class UserInteractionBuilder<T>
        implements UserInteractionBuildResponseRequired<T>, UserInteractionBuild<T> {
    private final UserInteractionType type;
    private final T payload;
    private boolean userResponseRequired = false;
    private String customResponseKey = null;

    UserInteractionBuilder(UserInteractionType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    @Override
    public UserInteractionBuild<T> noUserResponseRequired() {
        this.userResponseRequired = false;
        return this;
    }

    @Override
    public UserInteractionBuild<T> userResponseRequired() {
        this.userResponseRequired = true;
        return this;
    }

    @Override
    public UserInteractionBuild<T> userResponseRequired(String customResponseKey) {
        this.userResponseRequired = true;
        this.customResponseKey = customResponseKey;
        return this;
    }

    @Override
    public UserInteraction<T> build() {
        return new UserInteraction<>(
                this.type, this.payload, this.userResponseRequired, this.customResponseKey);
    }
}
