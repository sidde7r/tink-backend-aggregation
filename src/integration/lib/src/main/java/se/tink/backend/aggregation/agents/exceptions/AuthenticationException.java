package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

/**
 * What is authentication?
 *
 * <p>Authentication is the process of ascertaining that somebody really is who he claims to be.
 *
 * <p>For more info see:
 * http://stackoverflow.com/questions/6556522/authentication-versus-authorization
 */
public abstract class AuthenticationException extends AgentException {
    AuthenticationException(AgentError error) {
        super(error);
    }

    AuthenticationException(AgentError error, Throwable cause) {
        super(error, cause);
    }

    AuthenticationException(AgentError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    AuthenticationException(AgentError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public AuthenticationException(AgentError error, String internalMessage) {
        super(error, internalMessage);
    }
}
