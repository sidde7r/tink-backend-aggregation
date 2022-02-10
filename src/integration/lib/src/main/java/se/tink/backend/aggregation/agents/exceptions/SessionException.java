package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class SessionException extends AuthenticationException {

    public SessionException(SessionError error) {
        super(error);
    }

    public SessionException(SessionError error, Throwable cause) {
        super(error, cause);
    }

    public SessionException(SessionError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public SessionException(SessionError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public SessionException(SessionError sessionError, String internalMessage) {
        super(sessionError, internalMessage);
    }

    @Override
    public SessionError getError() {
        return getError(SessionError.class);
    }
}
