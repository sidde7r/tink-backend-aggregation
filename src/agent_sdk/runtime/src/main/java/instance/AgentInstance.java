package se.tink.agent.runtime.instance;

import com.google.common.base.Preconditions;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import java.util.Optional;
import se.tink.agent.runtime.environment.AgentEnvironment;

public class AgentInstance {
    private final AgentEnvironment environment;
    private final Class<?> agentClass;
    private final Object instance;

    public AgentInstance(AgentEnvironment environment, Class<?> agentClass)
            throws AgentInstantiationException {
        this.environment =
                Preconditions.checkNotNull(environment, "Agent environment cannot be null.");
        this.agentClass = Preconditions.checkNotNull(agentClass, "Agent class cannot be null.");
        this.instance = instantiateAgentClass(environment, agentClass);
    }

    public AgentEnvironment getEnvironment() {
        return environment;
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

    private Object instantiateAgentClass(AgentEnvironment environment, Class<?> agentClass)
            throws AgentInstantiationException {
        AgentEnvironmentModule environmentModule = new AgentEnvironmentModule(environment);

        final Injector injector = Guice.createInjector(environmentModule);
        try {
            return injector.getInstance(agentClass);
        } catch (ConfigurationException | ProvisionException e) {
            throw new AgentInstantiationException(e);
        }
    }

    public static class AgentInstantiationException extends Exception {
        public AgentInstantiationException(Throwable cause) {
            super(cause);
        }
    }
}
