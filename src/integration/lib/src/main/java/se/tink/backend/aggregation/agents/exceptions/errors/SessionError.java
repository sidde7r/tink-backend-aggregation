package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.libraries.i18n.LocalizableKey;

public enum SessionError implements AgentError {
    SESSION_EXPIRED(
            new LocalizableKey(
                    "For safety reasons you have been logged out. Please login again to continue.")),
    SESSION_ALREADY_ACTIVE(
            new LocalizableKey(
                    "There is already an active session on this account. Please try again."));

    private LocalizableKey userMessage;

    SessionError(LocalizableKey userMessage) {
        this.userMessage = userMessage;
    }

    @Override
    public SessionException exception() {
        return new SessionException(this);
    }

    @Override
    public LocalizableKey userMessage() {
        return userMessage;
    }

    @Override
    public SessionException exception(LocalizableKey userMessage) {
        return new SessionException(this, userMessage);
    }
}
