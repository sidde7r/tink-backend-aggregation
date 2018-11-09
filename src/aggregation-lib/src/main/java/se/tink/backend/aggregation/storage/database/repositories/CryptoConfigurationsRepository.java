package se.tink.backend.aggregation.storage.database.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfigurationId;

public interface CryptoConfigurationsRepository extends JpaRepository<CryptoConfiguration, CryptoConfigurationId> {
}
