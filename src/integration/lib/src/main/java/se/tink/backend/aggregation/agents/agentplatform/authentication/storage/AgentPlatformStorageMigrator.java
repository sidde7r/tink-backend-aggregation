package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public interface AgentPlatformStorageMigrator {

    AgentAuthenticationPersistedData migrate(PersistentStorage ps);
}
