package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum ThirdPartyAppError implements AgentError {
    CANCELLED(new LocalizableKey("Authentication was cancelled. Please try again.")),
    TIMED_OUT(new LocalizableKey("Authentication timed out.")),
    ALREADY_IN_PROGRESS(new LocalizableKey("Another client is already trying to sign in.")),
    AUTHENTICATION_ERROR(new LocalizableKey("Authentication error."));

    private final LocalizableKey userMessage;

    ThirdPartyAppError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
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
