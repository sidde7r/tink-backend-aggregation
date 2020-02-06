package se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory;

import se.tink.backend.aggregation.agents.CompositeAgentContext;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentContextProviderFactoryImpl implements AgentContextProviderFactory {

    @Override
    public AgentContextProvider createAgentContextProvider(
            CredentialsRequest credentialsRequest, CompositeAgentContext context) {
        return new AgentContextProviderImpl(credentialsRequest, context);
    }
}
