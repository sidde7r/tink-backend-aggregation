package se.tink.backend.product.execution.unit.agents.exceptions;

import se.tink.backend.product.execution.unit.agents.exceptions.errors.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class MultiFactorAuthenticationException extends AuthenticationException {
    MultiFactorAuthenticationException(AgentError error) {
        super(error);
    }

    MultiFactorAuthenticationException(AgentError error, LocalizableKey userMessage) {
        super(error, userMessage);
    }
}
