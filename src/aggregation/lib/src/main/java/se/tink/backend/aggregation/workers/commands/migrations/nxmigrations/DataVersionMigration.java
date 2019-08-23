package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations;

import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class DataVersionMigration {

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

    public MigrationResult migrate(CredentialsRequest request, ClientInfo clientInfo) {
        final int dataVersion = request.getCredentials().getDataVersion();

        if (dataVersion > getMigrationFromVersion()) {
            return MigrationResult.SKIP;
        } else if (dataVersion == getMigrationFromVersion()) {
            return migrateData(request);
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Invalid migration chain - migration from data version v%d to v%d received credential of version %d.",
                            getMigrationFromVersion(), getMigrationToVersion(), dataVersion));
        }
    }

    protected abstract MigrationResult migrateData(CredentialsRequest request);
}
