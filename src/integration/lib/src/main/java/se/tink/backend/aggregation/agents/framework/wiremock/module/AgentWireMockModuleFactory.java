package se.tink.backend.aggregation.agents.framework.wiremock.module;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.module.AgentRequestScopeModule;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentWireMockModuleFactory implements AgentModuleFactory {

    private final WireMockConfiguration wireMockConfiguration;
    private final FakeBankSocket fakeBankSocket;

    @Inject
    private AgentWireMockModuleFactory(
            WireMockConfiguration wireMockConfiguration, FakeBankSocket fakeBankSocket) {
        this.wireMockConfiguration = wireMockConfiguration;
        this.fakeBankSocket = fakeBankSocket;
    }

    @Override
    public Set<Module> getAgentModules(
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
