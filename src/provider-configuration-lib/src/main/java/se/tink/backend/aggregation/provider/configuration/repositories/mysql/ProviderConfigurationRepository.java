package se.tink.backend.aggregation.provider.configuration.repositories.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.provider.configuration.repositories.ProviderConfiguration;

public interface ProviderConfigurationRepository extends JpaRepository<ProviderConfiguration, String>,
        ProviderConfigurationRepositoryCustom {
}
