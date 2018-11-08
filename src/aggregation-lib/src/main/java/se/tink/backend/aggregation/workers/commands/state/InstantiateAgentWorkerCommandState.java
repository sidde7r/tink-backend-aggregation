package se.tink.backend.aggregation.workers.commands.state;

import javax.inject.Inject;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

public class InstantiateAgentWorkerCommandState {
    private AgentFactory agentFactory;

    @Inject
    public InstantiateAgentWorkerCommandState(AgentsServiceConfiguration configuration) {
        agentFactory = new AgentFactory(configuration);
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }
}
