package se.tink.backend.aggregation.provider.configuration.config;

import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.guice.configuration.RepositoryModule;

public class ProviderRepositoryModule extends RepositoryModule {
    public ProviderRepositoryModule(DatabaseConfiguration databaseConfiguration) {
        super(databaseConfiguration);
    }

    protected void bindRepositories() {
        bindSpringBean(ProviderStatusConfigurationRepository.class);
    }
}
