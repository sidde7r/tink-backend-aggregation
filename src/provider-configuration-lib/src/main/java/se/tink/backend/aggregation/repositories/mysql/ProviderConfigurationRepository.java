package se.tink.backend.aggregation.repositories.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ProviderConfiguration;

public interface ProviderConfigurationRepository extends JpaRepository<ProviderConfiguration, String>,
        ProviderConfigurationRepositoryCustom {
}
