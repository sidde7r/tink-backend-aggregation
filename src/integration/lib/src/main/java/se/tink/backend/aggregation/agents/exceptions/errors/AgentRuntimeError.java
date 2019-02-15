package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.AgentRuntimeExceptionImpl;
import se.tink.libraries.i18n.LocalizableKey;

public interface AgentRuntimeError extends AgentBaseError {
    @Override
    String name();
    @Override
    LocalizableKey userMessage();
    @Override
    AgentRuntimeExceptionImpl exception();
    @Override
    AgentRuntimeExceptionImpl exception(LocalizableKey userMessage);
}
