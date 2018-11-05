package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.configurations.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.CryptoConfigurationsRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

import java.util.Map;
import java.util.stream.Collectors;

/*
    database configuration for running aggregation locally.
    intended for cluster hosting multiple clients structure
 */
public class AggregationDevelopmentMultiClientRepositoryModule extends RepositoryModule {
    public AggregationDevelopmentMultiClientRepositoryModule(DatabaseConfiguration databaseConfiguration) {
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
}
