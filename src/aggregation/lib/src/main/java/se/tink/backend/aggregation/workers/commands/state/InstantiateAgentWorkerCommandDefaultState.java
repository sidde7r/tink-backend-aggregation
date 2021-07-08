package se.tink.backend.aggregation.workers.commands.state;

import javax.inject.Inject;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;

public class InstantiateAgentWorkerCommandDefaultState
        implements InstantiateAgentWorkerCommandState {

    private final AgentFactory agentFactory;

    @Inject
    public InstantiateAgentWorkerCommandDefaultState(AgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }

    @Override
    public void doRightBeforeInstantiation(String providerName, String credentialsId) {
        // noop
    }

    @Override
    public void doAtInstantiationPostProcess() {
        // noop
    }
}
