package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.libraries.i18n.LocalizableKey;

/**
 * What is authorization?
 *
 * Authorization refers to rules that determine who is allowed to do what. E.g. Adam may be authorized to create
 * and delete databases, while Usama is only authorised to read.
 *
 * For more info see: http://stackoverflow.com/questions/6556522/authentication-versus-authorization
 */
public class AuthorizationException extends AgentExceptionImpl {
    private final AuthorizationError error;

    public AuthorizationException(AuthorizationError error) {
        super(error);
        this.error = error;
    }

    public AuthorizationException(AuthorizationError error, LocalizableKey userMessage) {
        super(error, userMessage);
        this.error = error;
    }

    @Override
    public AuthorizationError getError() {
        return error;
    }
}
