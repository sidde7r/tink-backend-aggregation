package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public enum ThirdPartyAppError implements AgentError {
    CANCELLED("Authentication cancelled by the app. Please try again."),
    TIMED_OUT("Authentication timed out."),
    ALREADY_IN_PROGRESS("Another client is already trying to sign in."),
    AUTHENTICATION_ERROR("Authentication error.");

    private final LocalizableKey userMessage;

    ThirdPartyAppError(String userMessage) {
        this.userMessage = new LocalizableKey(userMessage);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public ThirdPartyAppException exception() {
        return new ThirdPartyAppException(this);
    }

    @Override
    public ThirdPartyAppException exception(String internalMessage) {
        return new ThirdPartyAppException(this, internalMessage);
    }

    @Override
    public ThirdPartyAppException exception(Throwable cause) {
        return new ThirdPartyAppException(this, cause);
    }

    @Override
    public ThirdPartyAppException exception(LocalizableKey userMessage) {
        return new ThirdPartyAppException(this, userMessage);
    }

    @Override
    public ThirdPartyAppException exception(LocalizableKey userMessage, Throwable cause) {
        return new ThirdPartyAppException(this, userMessage, cause);
    }
}
