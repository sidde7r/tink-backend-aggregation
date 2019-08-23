package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.implementations.avanza;

import se.tink.backend.aggregation.workers.commands.migrations.nxmigrations.DataVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class Avanza_v1_v2 extends DataVersionMigration {

    @Override
    public int getMigrationFromVersion() {
        return 1;
    }

    @Override
    protected MigrationResult migrateData(CredentialsRequest request) {
        return MigrationResult.MIGRATED;
    }
}
