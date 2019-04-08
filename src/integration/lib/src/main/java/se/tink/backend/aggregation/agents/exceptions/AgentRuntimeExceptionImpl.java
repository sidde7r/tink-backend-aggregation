package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentBaseError;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class AgentRuntimeExceptionImpl extends RuntimeException implements AgentException {
    private final LocalizableKey userMessage;

    AgentRuntimeExceptionImpl(AgentBaseError error) {
        this(error, error.userMessage());
    }

    AgentRuntimeExceptionImpl(AgentBaseError error, LocalizableKey userMessage) {
        super(String.format("Cause: %s.%s", error.getClass().getSimpleName(), error.name()));

        this.userMessage = userMessage;
    }

    public LocalizableKey getUserMessage() {
        return userMessage;
    }
}
