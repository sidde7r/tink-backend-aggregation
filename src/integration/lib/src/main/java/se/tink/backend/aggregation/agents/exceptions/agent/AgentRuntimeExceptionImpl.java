package se.tink.backend.aggregation.agents.exceptions.agent;

import java.util.Objects;
import se.tink.libraries.i18n.LocalizableKey;

public abstract class AgentRuntimeExceptionImpl extends RuntimeException implements AgentException {
    private final LocalizableKey userMessage;
    private final AgentBaseError error;

    public AgentRuntimeExceptionImpl(AgentBaseError error) {
        this(error, error.userMessage());
    }

    public AgentRuntimeExceptionImpl(AgentBaseError error, Throwable cause) {
        this(error, error.userMessage(), cause);
    }

    public AgentRuntimeExceptionImpl(AgentBaseError error, LocalizableKey userMessage) {
        this(error, userMessage, null);
    }

    public AgentRuntimeExceptionImpl(AgentBaseError error, LocalizableKey userMessage, Throwable cause) {
        super(String.format("Cause: %s.%s", error.getClass().getSimpleName(), error.name()), cause);
        this.error = Objects.requireNonNull(error, "error object is mandatory");
        this.userMessage = userMessage;
    }

    public AgentRuntimeExceptionImpl(AgentBaseError error, String internalMessage) {
        super(internalMessage);
        this.error = Objects.requireNonNull(error, "error object is mandatory");
        this.userMessage = error.userMessage();
    }

    public LocalizableKey getUserMessage() {
        return userMessage;
    }

    protected <E> E getError(Class<E> clazz) {
        return clazz.cast(error);
    }
}
