package se.tink.backend.aggregation.agents.agentfactory.iface;

import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** Capable of producing agents. */
public interface AgentFactory {

    /** Creates an agent from a credentials request and an agent context. */
    Agent create(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException;

    /** Creates an agent SDK instance from a credentials request and an agent context. */
    AgentInstance createAgentSdkInstance(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException;
}
