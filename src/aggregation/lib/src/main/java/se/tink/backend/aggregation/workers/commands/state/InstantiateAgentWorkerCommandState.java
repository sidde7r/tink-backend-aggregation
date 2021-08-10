package se.tink.backend.aggregation.workers.commands.state;

import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;

public interface InstantiateAgentWorkerCommandState {

    AgentFactory getAgentFactory();

    /** Invoked right before the agent is instantiated. */
    void doRightBeforeInstantiation(String providerName, String credentialsId);

    /** Invoked when the resources allocated during instantiation need to be cleared. */
    void doAtInstantiationPostProcess();
}
