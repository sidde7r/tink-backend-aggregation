package se.tink.agent.runtime.instance;

import java.util.Optional;

public class AgentInstance {
    private final Class<?> agentClass;
    private final Object instance;

    public AgentInstance(Class<?> agentClass, Object instance) {
        this.agentClass = agentClass;
        this.instance = instance;
    }

    public boolean isInstanceOf(Class<?> cls) {
        return cls.isAssignableFrom(this.agentClass);
    }

    public <T> Optional<T> instanceOf(Class<T> cls) {
        if (!this.isInstanceOf(cls)) {
            return Optional.empty();
        }

        // It's not unchecked, the check is the line before.
        @SuppressWarnings("unchecked")
        T tCast = (T) this.instance;
        return Optional.of(tCast);
    }
}
