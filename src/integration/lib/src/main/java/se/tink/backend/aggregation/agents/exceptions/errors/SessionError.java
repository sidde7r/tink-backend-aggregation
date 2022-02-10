package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum SessionError implements AgentError {
    SESSION_EXPIRED(
            new LocalizableKey(
                    "For safety reasons you have been logged out. Please login again to continue.")),
    SESSION_ALREADY_ACTIVE(
            new LocalizableKey(
                    "There is already an active session on this account. Please try again.")),
    // These errors are duplicates from BankServiceError. They will be removed when all
    // agents start to use these errors from SessionError
    CONSENT_EXPIRED(new LocalizableKey("The consent has been expired. ")),
    CONSENT_INVALID(new LocalizableKey("The consent is invalid. ")),
    CONSENT_REVOKED_BY_USER(new LocalizableKey("The consent has been revoked by the user. ")),
    CONSENT_REVOKED(new LocalizableKey("The consent has been revoked by the bank. "));

    private LocalizableKey userMessage;

    SessionError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public SessionException exception() {
        return new SessionException(this);
    }

    @Override
    public SessionException exception(String internalMessage) {
        return new SessionException(this, internalMessage);
    }

    @Override
    public SessionException exception(Throwable cause) {
        return new SessionException(this, cause);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public SessionException exception(LocalizableKey userMessage) {
        return new SessionException(this, userMessage);
    }

    @Override
    public SessionException exception(LocalizableKey userMessage, Throwable cause) {
        return new SessionException(this, userMessage, cause);
    }
}
