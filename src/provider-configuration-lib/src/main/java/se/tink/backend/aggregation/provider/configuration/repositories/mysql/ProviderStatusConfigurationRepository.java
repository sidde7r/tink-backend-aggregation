package se.tink.backend.aggregation.provider.configuration.repositories.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.provider.configuration.repositories.ProviderStatusConfiguration;

public interface ProviderStatusConfigurationRepository extends JpaRepository<ProviderStatusConfiguration, String>,
        ProviderStatusConfigurationRepositoryCustom {
}
