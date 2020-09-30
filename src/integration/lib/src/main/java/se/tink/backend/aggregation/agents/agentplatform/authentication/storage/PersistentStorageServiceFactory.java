package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class PersistentStorageServiceFactory {

    public static PersistentStorageService create(Agent agent, PersistentStorage ps) {
        if (agent instanceof AgentPlatformStorageMigration) {
            return new UpgradingPersistentStorageService(
                    ps, ((AgentPlatformStorageMigration) agent).getMigrator());
        } else {
            return new PersistentStorageService(ps);
        }
    }
}
