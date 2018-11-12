package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.stream.Collectors;

import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.storage.database.converter.HostConfigurationConverter;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
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
        bindSpringBean(CryptoConfigurationsRepository.class);
        bindSpringBean(ClientConfigurationsRepository.class);
        bindSpringBean(AggregatorConfigurationsRepository.class);
        bindSpringBean(ClusterConfigurationsRepository.class);
    }

    @Provides
    @Singleton
    @Named("clusterHostConfigurations")
    public Map<String, ClusterHostConfiguration> provideClusterHostConfigurations(ClusterHostConfigurationRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterHostConfiguration::getClusterId, x -> x)
        );
    }

    @Provides
    @Singleton
    @Named("clusterConfigurations")
    public Map<String, ClusterConfiguration> provideClusterConfigurations(ClusterConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterConfiguration::getClusterId, x -> x)
        );
    }
}
