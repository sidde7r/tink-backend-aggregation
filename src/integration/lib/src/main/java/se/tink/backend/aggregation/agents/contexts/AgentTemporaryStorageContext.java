package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

public interface AgentTemporaryStorageContext {

    AgentTemporaryStorage getAgentTemporaryStorage();

    void setAgentTemporaryStorage(AgentTemporaryStorage agentTemporaryStorage);
}
