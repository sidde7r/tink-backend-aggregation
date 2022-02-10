package se.tink.backend.aggregation.agents.exceptions.agent;

import se.tink.libraries.i18n_aggregation.LocalizableKey;

public interface AgentError extends AgentBaseError {
    @Override
    String name();

    @Override
    LocalizableKey userMessage();

    @Override
    AgentException exception();

    @Override
    AgentException exception(String internalMessage);

    @Override
    AgentException exception(Throwable cause);

    @Override
    AgentException exception(LocalizableKey userMessage);

    @Override
    AgentException exception(LocalizableKey userMessage, Throwable cause);
}
