package se.tink.backend.aggregation.agents.exceptions.agent;

import java.util.Objects;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class AgentExceptionImpl extends RuntimeException implements AgentException {
    private final LocalizableKey userMessage;
    private final AgentError error;

    public AgentExceptionImpl(AgentError error) {
        this(error, error.userMessage());
    }

    public AgentExceptionImpl(AgentError error, Throwable cause) {
        this(error, error.userMessage(), cause);
    }

    public AgentExceptionImpl(AgentError error, LocalizableKey userMessage) {
        this(error, userMessage, null);
    }

    public AgentExceptionImpl(AgentError error, String internalMessage) {
        super(internalMessage);
        this.error = Objects.requireNonNull(error, "error object is mandatory");
        this.userMessage = error.userMessage();
    }

    public AgentExceptionImpl(AgentError error, LocalizableKey userMessage, Throwable cause) {
        super(String.format("Cause: %s.%s", error.getClass().getSimpleName(), error.name()), cause);
        this.error = Objects.requireNonNull(error, "error object is mandatory");
        this.userMessage = userMessage;
    }

    public LocalizableKey getUserMessage() {
        return userMessage;
    }

    protected <E> E getError(Class<E> clazz) {
        return clazz.cast(error);
    }
}
