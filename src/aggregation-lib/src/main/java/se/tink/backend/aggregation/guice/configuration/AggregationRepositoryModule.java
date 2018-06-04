package se.tink.backend.aggregation.guice.configuration;

import se.tink.backend.common.repository.mysql.aggregation.AggregationCredentialsRepository;
import se.tink.backend.common.repository.mysql.aggregation.ClusterCryptoConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.ClusterHostConfigurationRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.ClusterProviderConfigurationRepository;
import se.tink.backend.common.repository.mysql.aggregation.ProviderConfigurationRepository;
import se.tink.backend.guice.configuration.RepositoryModule;

public class AggregationRepositoryModule extends RepositoryModule {
    public AggregationRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }

    @Override
    protected void bindRepositories() {
        bindSpringBean(AggregationCredentialsRepository.class);
        bindSpringBean(ClusterHostConfigurationRepository.class);
        bindSpringBean(ClusterCryptoConfigurationRepository.class);
        bindSpringBean(ClusterProviderConfigurationRepository.class);
        bindSpringBean(ProviderConfigurationRepository.class);
    }
}
