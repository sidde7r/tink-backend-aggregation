package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

/**
 * What is authentication?
 *
 * Authentication is the process of ascertaining that somebody really is who he claims to be.
 *
 * For more info see: http://stackoverflow.com/questions/6556522/authentication-versus-authorization
 */
public abstract class AuthenticationException extends AgentExceptionImpl {
    AuthenticationException(AgentError error) {
        super(error);
    }

    AuthenticationException(AgentError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }
}
