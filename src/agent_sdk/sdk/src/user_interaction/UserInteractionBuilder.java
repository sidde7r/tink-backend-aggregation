package se.tink.agent.sdk.user_interaction;

public class UserInteractionBuilder<T> {
    private final UserInteractionType type;
    private final T payload;
    private boolean userResponseRequired = false;
    private String customResponseKey = null;

    UserInteractionBuilder(UserInteractionType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public UserInteractionBuilder<T> userResponseRequired() {
        this.userResponseRequired = true;
        return this;
    }

    public UserInteractionBuilder<T> userResponseRequired(String customResponseKey) {
        this.userResponseRequired = true;
        this.customResponseKey = customResponseKey;
        return this;
    }

    public UserInteraction<T> build() {
        return new UserInteraction<>(
                this.type, this.payload, this.userResponseRequired, this.customResponseKey);
    }
}
