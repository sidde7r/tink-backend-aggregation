package se.tink.backend.aggregation.provider.configuration.storage.repositories;

import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;

import java.util.Optional;

public interface ProviderStatusConfigurationRepositoryCustom {
    Optional<ProviderStatusConfiguration> getProviderStatusConfiguration(String providerName);
}
