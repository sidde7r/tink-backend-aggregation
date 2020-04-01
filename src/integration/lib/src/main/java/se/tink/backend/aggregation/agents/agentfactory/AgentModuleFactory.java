package se.tink.backend.aggregation.agents.agentfactory;

import com.google.inject.Module;
import java.util.Set;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface AgentModuleFactory {

    Set<Module> getAgentModules(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration)
            throws ReflectiveOperationException;
}
