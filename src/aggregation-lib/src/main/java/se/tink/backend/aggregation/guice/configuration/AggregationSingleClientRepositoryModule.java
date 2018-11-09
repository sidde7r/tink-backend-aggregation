package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

/*
    database configuration for running aggregation in production environment.
    intended for cluster hosting only 1 client structure
 */
public class AggregationSingleClientRepositoryModule extends RepositoryModule {
    public AggregationSingleClientRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(ClusterHostConfigurationRepository.class);
        bindSpringBean(ClusterCryptoConfigurationRepository.class);
    }

    @Provides
    @Singleton
    @Named("clusterHostConfigurations")
    public Map<String, ClusterHostConfiguration> provideClusterHostConfigurations(ClusterHostConfigurationRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterHostConfiguration::getClusterId, x -> x)
        );
    }
}
