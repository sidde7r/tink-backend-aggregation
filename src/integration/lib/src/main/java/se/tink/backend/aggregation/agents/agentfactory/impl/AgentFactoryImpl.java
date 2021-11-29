package se.tink.backend.aggregation.agents.agentfactory.impl;

import com.google.common.base.Objects;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentFactoryImpl implements AgentFactory {
    private final AgentsServiceConfiguration configuration;
    private final AgentModuleFactory moduleLoader;

    @Inject
    private AgentFactoryImpl(
            AgentModuleFactory moduleLoader, AgentsServiceConfiguration configuration) {
        this.moduleLoader = moduleLoader;
        this.configuration = configuration;
    }

    /**
     * Creates agent from the className specified in the provider contained in the {@code request}
     * using one of the supported constructor signatures.
     *
     * @param request Request that the agent will serve.
     * @param context Context to execute the agent within.
     * @return An agent.
     * @throws ReflectiveOperationException If appropriate constructor or module can't be found.
     */
    @Override
    public Agent create(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {

        Class<? extends Agent> cls = getAgentClass(request.getCredentials(), request.getProvider());

        if (!AgentFactoryUtils.hasInjectAnnotatedConstructor(cls)) {
            throw new IllegalStateException("Required for agents to run @Inject-annotated ctor");
        }

        Set<Module> modules = moduleLoader.getAgentModules(cls, request, context, configuration);
        final Injector injector = Guice.createInjector(modules);
        final Agent agent = injector.getInstance(cls);
        agent.setConfiguration(configuration);
        return agent;
    }

    private static Class<? extends Agent> getAgentClass(Credentials credentials, Provider provider)
            throws ReflectiveOperationException {
        Class<? extends Agent> agentClass;

        // Check if this is demo account.
        if (credentials.isDemoCredentials()
                && !Objects.equal(credentials.getType(), CredentialsTypes.FRAUD)) {
            agentClass = AgentClassFactory.getAgentClass("demo.DemoAgent");
            credentials.setPassword("demo");
        } else {
            agentClass = AgentClassFactory.getAgentClass(provider);
        }

        return agentClass;
    }
}
