package se.tink.backend.aggregation.agents.exceptions.agent;

import se.tink.libraries.i18n.LocalizableKey;

public interface AgentError extends AgentBaseError {
    @Override
    String name();

    @Override
    LocalizableKey userMessage();

    @Override
    AgentExceptionImpl exception();

    @Override
    AgentExceptionImpl exception(Throwable cause);

    @Override
    AgentExceptionImpl exception(LocalizableKey userMessage);

    @Override
    AgentExceptionImpl exception(LocalizableKey userMessage, Throwable cause);
}
