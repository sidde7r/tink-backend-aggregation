package se.tink.backend.aggregation.configuration.guice.modules;

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
import src.agent_sdk.compatibility_layers.aggregation_service.test.modules.AgentSdkTestModule;

public final class AgentSystemTestModuleFactory implements AgentModuleFactory {

    private final WireMockConfiguration wireMockConfiguration;
    private final FakeBankSocket fakeBankSocket;
    private final AgentPackageModuleFactory agentPackageModuleFactory;

    @Inject
    private AgentSystemTestModuleFactory(
            AgentPackageModuleFactory agentPackageModuleFactory,
            WireMockConfiguration wireMockConfiguration,
            FakeBankSocket fakeBankSocket) {
        this.agentPackageModuleFactory = agentPackageModuleFactory;
        this.wireMockConfiguration = wireMockConfiguration;
        this.fakeBankSocket = fakeBankSocket;
    }

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
                        .with(getSystemTestModules(request, context, agentConfiguration)));
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

    private ImmutableSet<Module> getSystemTestModules(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentConfiguration) {

        return ImmutableSet.of(
                new AgentSystemTestComponentProviderModule(
                        request, context, agentConfiguration, fakeBankSocket),
                new AgentRequestScopeModule(request, context, agentConfiguration),
                new AgentSdkTestModule(),
                wireMockConfiguration.getAgentModule());
    }
}
