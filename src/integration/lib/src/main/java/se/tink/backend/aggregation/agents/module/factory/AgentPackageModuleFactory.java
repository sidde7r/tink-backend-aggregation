package se.tink.backend.aggregation.agents.module.factory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.agentfactory.AgentModuleFactory;
import se.tink.backend.aggregation.agents.module.AgentComponentProviderModule;
import se.tink.backend.aggregation.agents.module.AgentRequestScopeModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentPackageModuleFactory implements AgentModuleFactory {

    @Override
    public Set<Module> getAgentModules(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {

        return ImmutableSet.<Module>builder()
                .add(new AgentComponentProviderModule())
                .add(new AgentRequestScopeModule(request, context, configuration))
                .build();
    }
}
