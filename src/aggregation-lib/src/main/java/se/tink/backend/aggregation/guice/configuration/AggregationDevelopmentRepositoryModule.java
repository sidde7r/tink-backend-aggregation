package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.common.config.AggregationDevelopmentConfiguration;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

public class AggregationDevelopmentRepositoryModule extends RepositoryModule {
    private AggregationDevelopmentConfiguration developmentConfiguration;

    public AggregationDevelopmentRepositoryModule(DatabaseConfiguration databaseConfiguration,
            AggregationDevelopmentConfiguration developmentConfiguration) {
        super(databaseConfiguration);
        this.developmentConfiguration = developmentConfiguration;
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(ClusterHostConfigurationRepository.class);
        bindSpringBean(ClusterCryptoConfigurationRepository.class);
    }

    @Provides
    @Singleton
    @Named("clusterHostConfigurations")
    public Map<String, ClusterHostConfiguration> provideCategoryCodesById(
            ClusterHostConfigurationRepository repository) {

        Map<String, ClusterHostConfiguration> clusterHostConfigurations = repository.findAll().stream()
                .collect(Collectors.toMap(ClusterHostConfiguration::getClusterId, x -> x));

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return clusterHostConfigurations;
        }

        ClusterHostConfiguration clusterHostConfiguration = developmentConfiguration.getClusterHostConfiguration();

        repository.save(clusterHostConfiguration);
        clusterHostConfigurations.put(clusterHostConfiguration.getClusterId(), clusterHostConfiguration);

        return clusterHostConfigurations;
    }
}
