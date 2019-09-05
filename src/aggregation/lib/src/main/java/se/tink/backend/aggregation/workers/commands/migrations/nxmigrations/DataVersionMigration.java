package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * Performs a migration of a credential from one <strong>data version</strong> to the next one.
 * Serves as the base class for all data version migrations.
 */
public abstract class DataVersionMigration {

    protected static final Logger log = LoggerFactory.getLogger(DataVersionMigration.class);

    public enum MigrationResult {
        /**
         * The data contents of this credential does not warrant migration, skip this migration
         * step. The credential's <code>dataVersion</code> property will be incremented without any
         * data being modified.
         */
        SKIP,

        /**
         * Migration of this credential's data was <strong>attempted but unsuccessful.</strong>
         * Abort the migration chain for this credential and do not save it to system.
         */
        ABORT,

        /**
         * The credential's data was successfully migrated, and it will be sent back to system to be
         * updated in the database.
         */
        MIGRATED,

        /**
         * The <code>dataVersion</code> of this credential is higher than this part of the migration
         * chain, neither the version number nor the contents will be modified.
         */
        TOO_HIGH
    }

    /**
     * @return The <code>dataVersion</code> from which this link in the migration chain handles. A
     *     link can never migrate more than one data version.
     */
    public abstract int getMigrationFromVersion();

    /**
     * Optionally determine whether the migration of this credential from version N to version N+1
     * is needed. If no migration is needed, the result of this part of the chain will be {@link
     * MigrationResult#SKIP}.
     *
     * <p>Default behavior is to migrate any credential with version number {@link
     * #getMigrationFromVersion()}.
     *
     * @return True if the credential's data is already migrated from version N to N+1, false if
     *     {@link #migrate(CredentialsRequest, ClientInfo)} should be executed.
     */
    protected boolean isAlreadyMigrated(CredentialsRequest request) {
        return request.getCredentials().getDataVersion() == getMigrationToVersion();
    }

    public final int getMigrationToVersion() {
        return getMigrationFromVersion() + 1;
    }

    public final Map<Account, String> migrate(CredentialsRequest request, ClientInfo clientInfo)
            throws MigrationFailedException, MigrationSkippedException {
        final int dataVersion = request.getCredentials().getDataVersion();

        if (dataVersion > getMigrationFromVersion() || isAlreadyMigrated(request)) {
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
     * @throws MigrationSkippedException If migration is not needed for this credential.
     */
    protected abstract Map<Account, String> migrateData(
            CredentialsRequest request, ClientInfo clientInfo)
            throws MigrationFailedException, MigrationSkippedException;
}
