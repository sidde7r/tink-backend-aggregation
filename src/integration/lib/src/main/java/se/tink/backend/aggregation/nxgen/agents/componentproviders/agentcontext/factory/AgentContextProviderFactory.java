package se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory;

import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface AgentContextProviderFactory {

    AgentContextProvider createAgentContextProvider(
            CredentialsRequest credentialsRequest, CompositeAgentContext context);
}
