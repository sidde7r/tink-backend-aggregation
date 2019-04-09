package se.tink.backend.aggregation.agents.exceptions.errors;

import se.tink.backend.aggregation.agents.exceptions.AgentExceptionImpl;
import se.tink.libraries.i18n.LocalizableKey;

public interface AgentError extends AgentBaseError {
    @Override
    String name();

    @Override
    LocalizableKey userMessage();

    @Override
    AgentExceptionImpl exception();

    @Override
    AgentExceptionImpl exception(LocalizableKey userMessage);
}
