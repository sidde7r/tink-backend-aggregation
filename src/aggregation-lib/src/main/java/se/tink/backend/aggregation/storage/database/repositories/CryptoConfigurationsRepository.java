package se.tink.backend.aggregation.storage.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;

public interface CryptoConfigurationsRepository extends JpaRepository<CryptoConfiguration, CryptoConfigurationId> {
}
