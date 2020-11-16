package se.tink.backend.aggregation.nxgen.agents;

import java.util.Optional;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agent.AgentVisitor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AgentPersistentStorageReceiverAgentVisitor implements AgentVisitor {

    private PersistentStorage persistentStorage;

    @Override
    public void visit(Agent agent) {
        if (agent instanceof SubsequentGenerationAgent) {
            SubsequentGenerationAgent subsequentGenerationAgent = (SubsequentGenerationAgent) agent;
            persistentStorage = subsequentGenerationAgent.getPersistentStorage();
        }
    }

    public Optional<PersistentStorage> getPersistentStorage() {
        return Optional.ofNullable(persistentStorage);
    }
}
