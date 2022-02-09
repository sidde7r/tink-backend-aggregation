package se.tink.backend.aggregation.agents.agentfactory;

import se.tink.agent.runtime.instance.AgentInstance;
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
    public AgentInstance createAgentSdkInstance(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException {
        return null;
    }
}
