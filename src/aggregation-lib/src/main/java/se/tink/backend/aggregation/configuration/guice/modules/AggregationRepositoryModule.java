package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.libraries.repository.config.DatabaseConfiguration;
import se.tink.libraries.repository.guice.configuration.RepositoryModule;

public class AggregationRepositoryModule extends RepositoryModule {
    public AggregationRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(CryptoConfigurationsRepository.class);
        bindSpringBean(ClientConfigurationsRepository.class);
        bindSpringBean(AggregatorConfigurationsRepository.class);
        bindSpringBean(ClusterConfigurationsRepository.class);
    }

    @Provides
    @Singleton
    @Named("clusterConfigurations")
    public Map<String, ClusterConfiguration> provideClusterConfigurations(ClusterConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterConfiguration::getClusterId, Function.identity())
        );
    }

    @Provides
    @Singleton
    @Named("aggregatorConfiguration")
    public Map<String, AggregatorConfiguration> providerAggregatorConfiguration(
            AggregatorConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(AggregatorConfiguration::getAggregatorId, Function.identity())
        );
    }

    @Provides
    @Singleton
    @Named("clientConfigurationByClientKey")
    public Map<String, ClientConfiguration> providerClientConfiguration(ClientConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClientConfiguration::getApiClientKey, Function.identity())
        );
    }

    @Provides
    @Singleton
    @Named("clientConfigurationByName")
    public Map<String, ClientConfiguration> providerClientConfigurationByName(ClientConfigurationsRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClientConfiguration::getClientName, Function.identity())
        );
    }
}
