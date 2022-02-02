package se.tink.agent.sdk.user_interaction.builder;

public interface UserInteractionBuildResponseRequired<T> {

    /**
     * The user interaction requires no response from the user. The framework will not wait for a
     * response before it continues with the next step.
     *
     * @return The next build step.
     */
    UserInteractionBuild<T> noUserResponseRequired();

    /**
     * The user interaction requires response from the user. The framework will wait for a response
     * before it continues with the next step.
     *
     * @return The next build step.
     */
    UserInteractionBuild<T> userResponseRequired();

    /**
     * The user interaction requires response from the user. The framework will wait for a response
     * before it continues with the next step.
     *
     * @param customResponseKey A custom response key. E.g. a state parameter in OAuth2 flows.
     * @return The next build step.
     */
    UserInteractionBuild<T> userResponseRequired(String customResponseKey);
}
