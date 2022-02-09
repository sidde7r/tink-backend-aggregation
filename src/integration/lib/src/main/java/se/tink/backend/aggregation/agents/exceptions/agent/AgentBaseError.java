package se.tink.backend.aggregation.agents.exceptions.agent;

import se.tink.libraries.i18n_aggregation.LocalizableKey;

public interface AgentBaseError {
    String name();

    LocalizableKey userMessage();

    Throwable exception();

    Throwable exception(String internalMessage);

    Throwable exception(Throwable cause);

    Throwable exception(LocalizableKey userMessage);

    Throwable exception(LocalizableKey userMessage, Throwable cause);
}
