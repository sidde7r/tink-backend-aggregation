package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum AuthorizationError implements AgentError {
    UNAUTHORIZED(new LocalizableKey("You are not authorized to use this service.")),
    NO_VALID_PROFILE(new LocalizableKey("You do not have a valid profile.")),
    ACCOUNT_BLOCKED(
            new LocalizableKey(
                    "Could not login to your bank. The access could be blocked. Please activate it in your bank app or contact your bank."));

    private final LocalizableKey userMessage;

    AuthorizationError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public AuthorizationException exception() {
        return new AuthorizationException(this);
    }

    @Override
    public AuthorizationException exception(String internalMessage) {
        return new AuthorizationException(this, internalMessage);
    }

    @Override
    public AuthorizationException exception(Throwable cause) {
        return new AuthorizationException(this);
    }

    @Override
    public AuthorizationException exception(LocalizableKey userMessage) {
        return new AuthorizationException(this, userMessage);
    }

    @Override
    public AuthorizationException exception(LocalizableKey userMessage, Throwable cause) {
        return new AuthorizationException(this, userMessage, cause);
    }
}
