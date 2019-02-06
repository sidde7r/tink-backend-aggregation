package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.i18n.LocalizableKey;

public class LoginException extends AuthenticationException {
    private LoginError error;

    public LoginException(LoginError error) {
        super(error);
        this.error = error;
    }

    public LoginException(LoginError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }

    @Override
    public LoginError getError() {
        return error;
    }
}
