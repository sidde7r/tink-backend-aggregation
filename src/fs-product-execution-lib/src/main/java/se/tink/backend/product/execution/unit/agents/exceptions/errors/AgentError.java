package se.tink.backend.product.execution.unit.agents.exceptions.errors;

import se.tink.backend.product.execution.unit.agents.exceptions.AgentExceptionImpl;
import se.tink.libraries.i18n.LocalizableKey;

public interface AgentError {
    String name();
    LocalizableKey userMessage();
    AgentExceptionImpl exception();
    AgentExceptionImpl exception(LocalizableKey userMessage);
}
