package se.tink.backend.product.execution.unit.agents.exceptions;

import se.tink.backend.product.execution.unit.agents.exceptions.errors.SessionError;
import se.tink.libraries.i18n.LocalizableKey;

public class SessionException extends AuthenticationException {
    private SessionError error;

    public SessionException(SessionError error) {
        super(error);
        this.error = error;
    }

    public SessionException(SessionError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }

    @Override
    public SessionError getError() {
        return error;
    }
}
