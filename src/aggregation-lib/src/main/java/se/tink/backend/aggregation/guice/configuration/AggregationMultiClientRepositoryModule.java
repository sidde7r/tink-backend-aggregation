package se.tink.backend.aggregation.guice.configuration;

import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

public class AggregationMultiClientRepositoryModule extends RepositoryModule {
    public AggregationMultiClientRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }
}
