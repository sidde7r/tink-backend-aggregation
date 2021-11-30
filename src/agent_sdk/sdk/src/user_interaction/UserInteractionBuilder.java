package se.tink.agent.sdk.user_interaction;

public class UserInteractionBuilder<T> {
    private final T payload;
    private boolean userResponseRequired = false;
    private String customResponseKey = null;

    UserInteractionBuilder(T payload) {
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
                this.payload, this.userResponseRequired, this.customResponseKey);
    }
}
