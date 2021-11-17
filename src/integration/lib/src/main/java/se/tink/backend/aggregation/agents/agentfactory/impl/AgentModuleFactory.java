package se.tink.backend.aggregation.agents.agentfactory.impl;

import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface AgentModuleFactory {

    Set<Module> getAgentModules(
            Class<? extends Agent> agentClass,
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration)
            throws ReflectiveOperationException;
}
