package se.tink.backend.aggregation.provider.configuration;

import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;

public class TestRepositoryModule extends AbstractModule {

    @Override
    protected void configure() {
        ProviderStatusConfigurationRepository test =
                mock(ProviderStatusConfigurationRepository.class);

        bind(ProviderStatusConfigurationRepository.class).toProvider(Providers.of(test));
    }
}
