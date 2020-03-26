package se.tink.backend.aggregation.workers.commands.state;

import javax.inject.Inject;
import se.tink.backend.aggregation.agents.agentfactory.AgentFactory;

public class InstantiateAgentWorkerCommandState {
    private final AgentFactory agentFactory;

    @Inject
    public InstantiateAgentWorkerCommandState(AgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }
}
