package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

/**
 * What is authorization?
 *
 * <p>Authorization refers to rules that determine who is allowed to do what. E.g. Adam may be
 * authorized to create and delete databases, while Usama is only authorised to read.
 *
 * <p>For more info see:
 * http://stackoverflow.com/questions/6556522/authentication-versus-authorization
 */
public class AuthorizationException extends AgentException {

    public AuthorizationException(AuthorizationError error) {
        super(error);
    }

    public AuthorizationException(AuthorizationError error, Throwable cause) {
        super(error, cause);
    }

    public AuthorizationException(AuthorizationError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public AuthorizationException(
            AuthorizationError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public AuthorizationException(AuthorizationError error, String internalMessage) {
        super(error, internalMessage);
    }

    @Override
    public AuthorizationError getError() {
        return getError(AuthorizationError.class);
    }
}
