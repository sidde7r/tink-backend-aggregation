package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UpgradingPersistentStorageService extends PersistentStorageService {

    // indicator that given storage was upgraded
    public static final String MARKER = "IS_TRANSFORMED_FOR_AGENT_PLATFORM_COMPATIBILITY";

    private final AgentPlatformStorageMigrator agentPlatformStorageMigrator;

    public UpgradingPersistentStorageService(
            PersistentStorage agentPersistentStorage,
            AgentPlatformStorageMigrator agentPlatformStorageMigrator) {
        super(agentPersistentStorage);
        this.agentPlatformStorageMigrator = agentPlatformStorageMigrator;
    }

    @Override
    public AgentAuthenticationPersistedData readFromAgentPersistentStorage() {
        if (wasNotMarkedAsUpgraded()) {
            return agentPlatformStorageMigrator.migrate(agentPersistentStorage);
        }
        return super.readFromAgentPersistentStorage();
    }

    @Override
    public void writeToAgentPersistentStorage(final AgentAuthenticationPersistedData data) {
        super.writeToAgentPersistentStorage(data);
        if (wasNotMarkedAsUpgraded()) {
            markAsUpgraded();
        }
    }

    private void markAsUpgraded() {
        agentPersistentStorage.put(MARKER, true);
    }

    private boolean wasNotMarkedAsUpgraded() {
        return !agentPersistentStorage.containsKey(MARKER);
    }
}
