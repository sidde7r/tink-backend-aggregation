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
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.libraries.repository.config.DatabaseConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.libraries.repository.guice.configuration.RepositoryModule;

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
        bindSpringBean(ClusterCryptoConfigurationRepository.class);
        bindSpringBean(CryptoConfigurationsRepository.class);
        bindSpringBean(ClientConfigurationsRepository.class);
        bindSpringBean(AggregatorConfigurationsRepository.class);
        bindSpringBean(ClusterConfigurationsRepository.class);
    }

    @Provides
    @Singleton
    @Named("clusterConfigurations")
    public Map<String, ClusterConfiguration> provideClusterConfigurations(ClusterConfigurationsRepository repository) {
        Map<String, ClusterConfiguration> clusterConfigurations =  repository.findAll().stream().collect(
                Collectors.toMap(ClusterConfiguration::getClusterId, Function.identity())
        );

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return clusterConfigurations;
        }

        ClusterConfiguration clusterConfiguration = developmentConfiguration.getClusterConfiguration();
        clusterConfigurations.put(clusterConfiguration.getClusterId(), clusterConfiguration);

        return clusterConfigurations;
    }

    @Provides
    @Singleton
    @Named("aggregatorConfiguration")
    // TODO change this later to get from service yml instead of database 
    public Map<String, AggregatorConfiguration> providerAggregatorConfiguration(
            AggregatorConfigurationsRepository repository) {
        Map<String, AggregatorConfiguration> aggregatorConfigurations = repository.findAll().stream().collect(
                Collectors.toMap(AggregatorConfiguration::getAggregatorId, Function.identity())
        );

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return aggregatorConfigurations;
        }

        AggregatorConfiguration aggregatorConfiguration = developmentConfiguration.getAggregatorConfiguration();
        aggregatorConfigurations.put(aggregatorConfiguration.getAggregatorId(), aggregatorConfiguration);

        return aggregatorConfigurations;
    }

    @Provides
    @Singleton
    @Named("clientConfigurationByClientKey")
    // TODO change this later for local development getting from service yml
    public Map<String, ClientConfiguration> providerClientConfiguration(ClientConfigurationsRepository repository) {
        Map<String, ClientConfiguration> clientConfigurations = repository.findAll().stream().collect(
                Collectors.toMap(ClientConfiguration::getApiClientKey, Function.identity())
        );

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return clientConfigurations;
        }

        ClientConfiguration clientConfiguration = developmentConfiguration.getClientConfiguration();
        clientConfigurations.put(clientConfiguration.getApiClientKey(), clientConfiguration);

        return clientConfigurations;
    }

    @Provides
    @Singleton
    @Named("clientConfigurationByName")
    public Map<String, ClientConfiguration> providerClientConfigurationByName(ClientConfigurationsRepository repository) {
        Map<String, ClientConfiguration> clientConfigurations = repository.findAll().stream().collect(
                Collectors.toMap(ClientConfiguration::getClientName, Function.identity())
        );

        if (developmentConfiguration == null || !developmentConfiguration.isValid()) {
            return clientConfigurations;
        }

        ClientConfiguration clientConfiguration = developmentConfiguration.getClientConfiguration();
        clientConfigurations.put(clientConfiguration.getClientName(), clientConfiguration);

        return clientConfigurations;
    }
}
