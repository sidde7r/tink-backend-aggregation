package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public abstract class MultiFactorAuthenticationException extends AuthenticationException {

    public MultiFactorAuthenticationException(AgentError error) {
        super(error);
    }

    public MultiFactorAuthenticationException(AgentError error, Throwable cause) {
        super(error, cause);
    }

    public MultiFactorAuthenticationException(AgentError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }

    public MultiFactorAuthenticationException(
            AgentError error, LocalizableKey userMessage, Throwable cause) {
        super(error, userMessage, cause);
    }

    public MultiFactorAuthenticationException(AgentError error, String internalMessage) {
        super(error, internalMessage);
    }
}
