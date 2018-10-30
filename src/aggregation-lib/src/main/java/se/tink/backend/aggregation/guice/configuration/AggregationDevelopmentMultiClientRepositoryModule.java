package se.tink.backend.aggregation.guice.configuration;

import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

// database configuration for running aggregation locally.
// intended for cluster hosting multiple clients structure
public class AggregationDevelopmentMultiClientRepositoryModule extends RepositoryModule {
    public AggregationDevelopmentMultiClientRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }
}
