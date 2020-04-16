package se.tink.backend.aggregation.agents.framework.compositeagenttest.base.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentProvider implements Provider<Agent> {

    private final AgentFactory agentFactory;
    private final CredentialsRequest request;
    private final AgentContext context;

    @Inject
    public AgentProvider(
            AgentFactory agentFactory, CredentialsRequest request, AgentContext context) {
        this.agentFactory = agentFactory;
        this.request = request;
        this.context = context;
    }

    @Override
    public Agent get() {
        try {
            return agentFactory.create(request, context);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
