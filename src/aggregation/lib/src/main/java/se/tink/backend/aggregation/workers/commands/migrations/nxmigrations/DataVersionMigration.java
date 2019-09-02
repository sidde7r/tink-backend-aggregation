package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class DataVersionMigration {

    public static final Logger log = LoggerFactory.getLogger(DataVersionMigration.class);

    public enum MigrationResult {
        SKIP,
        ABORT,
        MIGRATED,
        TOO_HIGH
    }

    public abstract int getMigrationFromVersion();

    public int getMigrationToVersion() {
        return getMigrationFromVersion() + 1;
    }

    public Map<Account, String> migrate(CredentialsRequest request, ClientInfo clientInfo)
            throws MigrationFailedException, MigrationSkippedException {
        final int dataVersion = request.getCredentials().getDataVersion();

        if (dataVersion > getMigrationFromVersion()) {
            throw new MigrationSkippedException();
        } else if (dataVersion == getMigrationFromVersion()) {
            return migrateData(request, clientInfo);

        } else {
            throw new IllegalStateException(
                    String.format(
                            "Invalid migration chain - migration from data version v%d to v%d received credential of version %d.",
                            getMigrationFromVersion(), getMigrationToVersion(), dataVersion));
        }
    }

    /**
     * Migrate a credential request to version {@link #getMigrationToVersion()}. The credential
     * itself can be modified freely, but for its accounts, only the updated Unique Identifier
     * (bankId) will be sent back in the system update.
     *
     * @param request Credentials Request (mutable) to migrate
     * @param clientInfo Cluster information
     * @return A Map with Account entity as key, and its new unique identifier (bankId) as value.
     * @throws MigrationFailedException If the migration failed to execute.
     */
    protected abstract Map<Account, String> migrateData(
            CredentialsRequest request, ClientInfo clientInfo) throws MigrationFailedException;
}
