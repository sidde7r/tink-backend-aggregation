package se.tink.backend.aggregation.provider.configuration.config;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

public class ProviderRepositoryModule extends RepositoryModule {
    public ProviderRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }

    protected void bindRepositories() {
        bindSpringBean(ClusterHostConfigurationRepository.class);
        bindSpringBean(ProviderStatusConfigurationRepository.class);
    }

    @Provides
    @Singleton
    @Named("clusterHostConfigurations")
    public Map<String, ClusterHostConfiguration> provideCategoryCodesById(ClusterHostConfigurationRepository repository) {
        return repository.findAll().stream().collect(
                Collectors.toMap(ClusterHostConfiguration::getClusterId, x -> x)
        );
    }
}
