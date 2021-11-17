package se.tink.backend.aggregation.agents.agentfactory.iface;

import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** Capable of producing agents. */
public interface AgentFactory {

    /** Creates an agent from a credentials request and an agent context. */
    Agent create(CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException;

    /** Creates an agent from an agent class, a credentials request and an agent context. */
    Agent create(
            Class<? extends Agent> agentClass, CredentialsRequest request, AgentContext context)
            throws ReflectiveOperationException;
}
