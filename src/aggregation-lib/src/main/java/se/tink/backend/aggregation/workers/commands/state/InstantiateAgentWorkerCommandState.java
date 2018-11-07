package se.tink.backend.aggregation.workers.commands.state;

import javax.inject.Inject;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;

public class InstantiateAgentWorkerCommandState {
    private AgentFactory agentFactory;

    @Inject
    public InstantiateAgentWorkerCommandState(ServiceConfiguration configuration) {
        agentFactory = new AgentFactory(configuration);
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }
}
