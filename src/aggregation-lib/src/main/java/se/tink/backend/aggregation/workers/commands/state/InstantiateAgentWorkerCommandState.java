package se.tink.backend.aggregation.workers.commands.state;

import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.common.ServiceContext;

public class InstantiateAgentWorkerCommandState {
    private AgentFactory agentFactory;

    public InstantiateAgentWorkerCommandState(ServiceContext serviceContext) {
        agentFactory = new AgentFactory(serviceContext.getConfiguration());
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }
}
