package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class LoginException extends AuthenticationException {

    public LoginException(LoginError error) {
        super(error);
    }

    public LoginException(LoginError error, Throwable cause) {
        super(error, cause);
    }

    public LoginException(LoginError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public LoginException(LoginError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public LoginException(LoginError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public LoginError getError() {
        return getError(LoginError.class);
    }
}
