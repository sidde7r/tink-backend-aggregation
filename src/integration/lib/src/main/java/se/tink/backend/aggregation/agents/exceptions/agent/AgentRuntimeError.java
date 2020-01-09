package se.tink.backend.aggregation.agents.exceptions.agent;

import se.tink.libraries.i18n.LocalizableKey;

public interface AgentRuntimeError extends AgentBaseError {
    @Override
    String name();

    @Override
    LocalizableKey userMessage();

    @Override
    AgentRuntimeExceptionImpl exception();

    @Override
    AgentRuntimeExceptionImpl exception(String internalMessage);

    @Override
    AgentRuntimeExceptionImpl exception(Throwable cause);

    @Override
    AgentRuntimeExceptionImpl exception(LocalizableKey userMessage);

    @Override
    AgentRuntimeExceptionImpl exception(LocalizableKey userMessage, Throwable cause);
}
