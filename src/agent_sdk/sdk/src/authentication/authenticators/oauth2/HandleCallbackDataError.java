package se.tink.agent.sdk.authentication.authenticators.oauth2;

import se.tink.agent.sdk.user_interaction.UserResponseData;

public interface HandleCallbackDataError {
    /**
     * If the callback has non-standard parameters defining errors, throw an appropriate exception.
     * The default implementation does nothing, standard errors are handled separately.
     *
     * @param callbackData parameters to OAuth2 callback // @throws AuthenticationException
     */
    void handleCallbackDataError(UserResponseData callbackData);
}
