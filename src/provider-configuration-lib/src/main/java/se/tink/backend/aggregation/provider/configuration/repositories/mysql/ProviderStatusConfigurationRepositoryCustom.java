package se.tink.backend.aggregation.provider.configuration.repositories.mysql;

import se.tink.backend.aggregation.provider.configuration.repositories.ProviderStatusConfiguration;

import java.util.Optional;

public interface ProviderStatusConfigurationRepositoryCustom {
    Optional<ProviderStatusConfiguration> getProviderStatusConfiguration(String providerName);
}
