package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class AgentExceptionImpl extends Exception implements AgentException {
    private final LocalizableKey userMessage;

    AgentExceptionImpl(AgentError error) {
        this(error, error.userMessage());
    }

    AgentExceptionImpl(AgentError error, LocalizableKey userMessage) {
        super(String.format("Cause: %s.%s",
                error.getClass().getSimpleName(),
                error.name()));

        this.userMessage = userMessage;
    }

    public LocalizableKey getUserMessage() {
        return userMessage;
    }
}
