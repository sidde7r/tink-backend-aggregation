package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UpgradingPersistentStorageService extends PersistentStorageService {

    private static final Logger LOG =
            LoggerFactory.getLogger(UpgradingPersistentStorageService.class);

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
            AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                    agentPlatformStorageMigrator.migrate(agentPersistentStorage);
            LOG.info("Persistent storage to the Agent Platform model has been migrated");
            return agentAuthenticationPersistedData;
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
