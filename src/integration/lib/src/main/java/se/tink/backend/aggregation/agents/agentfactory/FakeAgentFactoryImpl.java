package se.tink.backend.aggregation.agents.agentfactory;

import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class FakeAgentFactoryImpl implements AgentFactory {

    @Override
    public Agent create(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {
        return null;
    }

    @Override
    public Agent create(
            Class<? extends Agent> agentClass, CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {
        return null;
    }
}
