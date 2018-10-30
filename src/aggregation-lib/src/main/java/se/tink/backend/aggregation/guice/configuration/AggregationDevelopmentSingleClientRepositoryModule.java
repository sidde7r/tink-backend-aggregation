package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.common.config.AggregationDevelopmentConfiguration;
import se.tink.backend.common.config.ConfigurationsDao;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.AggregationConfigurations;
import se.tink.backend.common.repository.mysql.aggregation.aggregatorconfigurations.AggregatorConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clientconfigurations.ClientConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterconfigurations.ClusterConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.cryptoconfigurations.CryptoConfigurationsRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

/*
    database configuration for running aggregation locally.
    intended for cluster hosting only 1 client structure
 */
public class AggregationDevelopmentSingleClientRepositoryModule extends RepositoryModule {
    private AggregationDevelopmentConfiguration developmentConfiguration;

    AggregationDevelopmentSingleClientRepositoryModule(DatabaseConfiguration databaseConfiguration,
                                                       AggregationDevelopmentConfiguration developmentConfiguration) {
        super(databaseConfiguration);
        this.developmentConfiguration = developmentConfiguration;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(DevelopmentConfigurationSeeder.class).in(Scopes.SINGLETON);
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(ClusterHostConfigurationRepository.class);
        bindSpringBean(ClusterCryptoConfigurationRepository.class);
        bindSpringBean(CryptoConfigurationsRepository.class);
        bindSpringBean(ClientConfigurationsRepository.class);
        bindSpringBean(AggregatorConfigurationsRepository.class);
        bindSpringBean(ClusterConfigurationsRepository.class);
        bind(ConfigurationsDao.class).to(AggregationConfigurations.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    @Named("clusterHostConfigurations")
    public Map<String, ClusterHostConfiguration> provideClusterHostConfigurations(
            ClusterHostConfigurationRepository repository) {

        Map<String, ClusterHostConfiguration> clusterHostConfigurations = repository.findAll().stream()
                .collect(Collectors.toMap(ClusterHostConfiguration::getClusterId, x -> x));

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return clusterHostConfigurations;
        }

        ClusterHostConfiguration clusterHostConfiguration = developmentConfiguration.getClusterHostConfiguration();
        clusterHostConfigurations.put(clusterHostConfiguration.getClusterId(), clusterHostConfiguration);

        return clusterHostConfigurations;
    }
}
