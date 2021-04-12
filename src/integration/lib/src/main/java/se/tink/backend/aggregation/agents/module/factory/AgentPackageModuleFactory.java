package se.tink.backend.aggregation.agents.module.factory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.module.AgentComponentProviderModule;
import se.tink.backend.aggregation.agents.module.AgentRequestScopeModule;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClassModule;
import se.tink.backend.aggregation.agents.module.loader.AgentDependencyModuleLoader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.events.guice.EventsModule;
import se.tink.libraries.events.guice.configuration.EventSubmitterConfiguration;

public final class AgentPackageModuleFactory implements AgentModuleFactory {

    private final AgentDependencyModuleLoader agentDependencyModuleLoader;

    @Inject
    private AgentPackageModuleFactory(AgentDependencyModuleLoader agentDependencyModuleLoader) {
        this.agentDependencyModuleLoader = agentDependencyModuleLoader;
    }

    /**
     * Gets modules needed to run the agent in production. It will load a set of default
     * dependencies as well as any modules specified in code>@AgentDependencyModules</code> on the
     * agent.
     *
     * @param request CredentialsRequest to bind.
     * @param context Context to bind.
     * @param configuration Configuration to bind.
     * @return Set of modules that specifies bindings for the agent instantiation.
     * @throws NoSuchMethodException if one or more modules does not have default constructor.
     */
    @Override
    public ImmutableSet<Module> getAgentModules(
            Class<? extends Agent> agentClass,
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration)
            throws NoSuchMethodException {

        return ImmutableSet.<Module>builder()
                .add(new AgentClassModule(agentClass))
                .add(new AgentComponentProviderModule())
                .add(
                        new EventsModule(
                                EventSubmitterConfiguration.of(
                                        "aggregation",
                                        configuration
                                                .getEndpoints()
                                                .getEventProducerServiceConfiguration())))
                .add(new AgentRequestScopeModule(request, context, configuration))
                .addAll(agentDependencyModuleLoader.getModulesFromAnnotation(agentClass))
                .build();
    }
}
