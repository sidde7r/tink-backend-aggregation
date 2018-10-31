package se.tink.backend.aggregation.guice.configuration;

import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

/*
    database configuration for running aggregation in production environment.
    intended for cluster hosting multiple clients structure
 */
public class AggregationMultiClientRepositoryModule extends RepositoryModule {
    public AggregationMultiClientRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }
}
