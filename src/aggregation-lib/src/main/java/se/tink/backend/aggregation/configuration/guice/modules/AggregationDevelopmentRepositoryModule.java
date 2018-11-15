package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.configuration.models.AggregationDevelopmentConfiguration;
import se.tink.backend.aggregation.configuration.DevelopmentConfigurationSeeder;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

public class AggregationDevelopmentRepositoryModule extends RepositoryModule {
    private AggregationDevelopmentConfiguration developmentConfiguration;

    AggregationDevelopmentRepositoryModule(DatabaseConfiguration databaseConfiguration,
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
    }

    @Provides
    @Singleton
    @Named("clusterHostConfigurations")
    public Map<String, ClusterHostConfiguration> provideClusterHostConfigurations(
            ClusterHostConfigurationRepository repository) {

        Map<String, ClusterHostConfiguration> clusterHostConfigurations = repository.findAll().stream()
                .collect(Collectors.toMap(ClusterHostConfiguration::getClusterId, Function.identity()));

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return clusterHostConfigurations;
        }

        ClusterHostConfiguration clusterHostConfiguration = developmentConfiguration.getClusterHostConfiguration();
        clusterHostConfigurations.put(clusterHostConfiguration.getClusterId(), clusterHostConfiguration);

        return clusterHostConfigurations;
    }

    @Provides
    @Singleton
    @Named("clusterConfigurations")
    // TODO change this later to get from service yml instead of database 
    public Map<String, ClusterConfiguration> provideClusterConfigurations(ClusterConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterConfiguration::getClusterId, Function.identity())
        );
    }

    @Provides
    @Singleton
    @Named("aggregatorConfiguration")
    // TODO change this later to get from service yml instead of database 
    public Map<String, AggregatorConfiguration> providerAggregatorConfiguration(
            AggregatorConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(AggregatorConfiguration::getAggregatorId, Function.identity())
        );
    }
}
