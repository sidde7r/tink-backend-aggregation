package se.tink.agent.runtime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.instance.ProductionModuleFactory;

public class AgentRuntime {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ImmutableMap<String, Class<?>> agentClasses;

    public AgentRuntime(ImmutableMap<String, Class<?>> agentClasses) {
        this.agentClasses = agentClasses;
    }

    public List<String> getAgentIds() {
        return this.agentClasses.keySet().asList();
    }

    public Optional<AgentInstance> newInstance(String agentId) throws AgentInstantiationException {
        Class<?> agentClass = this.agentClasses.get(agentId);
        if (Objects.isNull(agentClass)) {
            return Optional.empty();
        }

        Object agentInstance = instantiateAgentClass(agentClass);
        return Optional.of(new AgentInstance(agentClass, agentInstance));
    }

    private Object instantiateAgentClass(Class<?> agentClass) throws AgentInstantiationException {

        ImmutableSet<Module> modules = new ProductionModuleFactory().getAgentModules();
        final Injector injector = Guice.createInjector(modules);
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
