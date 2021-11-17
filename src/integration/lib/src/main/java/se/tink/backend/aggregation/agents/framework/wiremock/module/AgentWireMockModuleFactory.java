package se.tink.backend.aggregation.agents.framework.wiremock.module;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentModuleFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.module.AgentRequestScopeModule;
import se.tink.backend.aggregation.agents.module.factory.AgentPackageModuleFactory;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentWireMockModuleFactory implements AgentModuleFactory {

    private final WireMockConfiguration wireMockConfiguration;
    private final FakeBankSocket fakeBankSocket;
    private final AgentPackageModuleFactory agentPackageModuleFactory;

    @Inject
    private AgentWireMockModuleFactory(
            AgentPackageModuleFactory agentPackageModuleFactory,
            WireMockConfiguration wireMockConfiguration,
            FakeBankSocket fakeBankSocket) {
        this.agentPackageModuleFactory = agentPackageModuleFactory;
        this.wireMockConfiguration = wireMockConfiguration;
        this.fakeBankSocket = fakeBankSocket;
    }

    /**
     * Gets modules needed to run a WireMock test for this agent. It will load a set of default
     * dependencies as well as any modules specified under the agent package. It will then override
     * all of these bindings with test specific bindings if they are specified.
     *
     * @param request CredentialsRequest to bind.
     * @param context Context to bind.
     * @param agentConfiguration Configuration to bind.
     * @return Set of modules that specifies bindings for the agent instantiation.
     * @throws ReflectiveOperationException If something went wrong when looking for modules in
     *     agent package.
     */
    @Override
    public ImmutableSet<Module> getAgentModules(
            Class<? extends Agent> agentClass,
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentConfiguration)
            throws ReflectiveOperationException {

        return ImmutableSet.of(
                Modules.override(
                                getProductionModules(
                                        agentClass, request, context, agentConfiguration))
                        .with(getWireMockModules(request, context, agentConfiguration)));
    }

    private ImmutableSet<Module> getProductionModules(
            Class<? extends Agent> agentClass,
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentConfiguration)
            throws ReflectiveOperationException {

        return agentPackageModuleFactory.getAgentModules(
                agentClass, request, context, agentConfiguration);
    }

    private ImmutableSet<Module> getWireMockModules(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentConfiguration) {

        return ImmutableSet.of(
                new AgentWireMockComponentProviderModule(
                        request,
                        context,
                        agentConfiguration,
                        wireMockConfiguration,
                        fakeBankSocket),
                new AgentRequestScopeModule(request, context, agentConfiguration),
                wireMockConfiguration.getAgentModule());
    }
}
