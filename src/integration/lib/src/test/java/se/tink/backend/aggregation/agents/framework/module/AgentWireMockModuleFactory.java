package se.tink.backend.aggregation.agents.framework.module;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.module.AgentRequestScopeModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentWireMockModuleFactory implements AgentModuleFactory {

    private final WireMockConfiguration wireMockConfiguration;

    @Inject
    public AgentWireMockModuleFactory(WireMockConfiguration wireMockConfiguration) {
        this.wireMockConfiguration = wireMockConfiguration;
    }

    @Override
    public Set<Module> getAgentModules(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentConfiguration) {

        return ImmutableSet.of(
                new AgentWireMockComponentProviderModule(
                        request, context, agentConfiguration, wireMockConfiguration),
                new AgentRequestScopeModule(request, context, agentConfiguration));
    }
}
