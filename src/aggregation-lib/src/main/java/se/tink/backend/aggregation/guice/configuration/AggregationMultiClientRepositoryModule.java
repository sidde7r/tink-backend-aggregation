package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.configurations.models.AggregatorConfiguration;
import se.tink.backend.aggregation.configurations.models.ClientConfiguration;
import se.tink.backend.aggregation.configurations.models.ClusterConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;
import se.tink.backend.aggregation.configurations.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.CryptoConfigurationsRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.aggregationcredentials.AggregationCredentialsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

import java.util.Map;
import java.util.stream.Collectors;

/*
    database configuration for running aggregation in production environment.
    intended for cluster hosting multiple clients structure
 */
public class AggregationMultiClientRepositoryModule extends RepositoryModule {
    public AggregationMultiClientRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(AggregationCredentialsRepository.class);
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
    @Named("cryptoConfiguration")
    public Map<String, CryptoConfiguration> provideCryptoConfigurations(CryptoConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(CryptoConfiguration::getCryptoId, x -> x)
        );
    }

    @Provides
    @Singleton
    @Named("clientConfiguration")
    public Map<String, ClientConfiguration> provideClientConfigurations(ClientConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClientConfiguration::getClientId, x -> x)
        );
    }

    @Provides
    @Singleton
    @Named("aggregatorConfiguration")
    public Map<String, AggregatorConfiguration> provideAggregatorConfigurations(AggregatorConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(AggregatorConfiguration::getAggregatorId, x -> x)
        );
    }

    @Provides
    @Singleton
    @Named("clusterConfiguration")
    public Map<String, ClusterConfiguration> provideClusterConfigurations(ClusterConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterConfiguration::getClusterId, x -> x)
        );
    }

}
