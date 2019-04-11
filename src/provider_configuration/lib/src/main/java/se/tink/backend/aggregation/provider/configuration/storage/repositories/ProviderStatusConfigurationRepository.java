package se.tink.backend.aggregation.provider.configuration.storage.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;

public interface ProviderStatusConfigurationRepository
        extends JpaRepository<ProviderStatusConfiguration, String>,
                ProviderStatusConfigurationRepositoryCustom {
    ProviderStatusConfiguration findOne(String providerName);

    ProviderStatusConfiguration save(ProviderStatusConfiguration s);

    void delete(String providerName);
}
