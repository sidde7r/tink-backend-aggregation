package se.tink.backend.aggregation.storage.database.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;

public interface CryptoConfigurationsRepository extends JpaRepository<CryptoConfiguration, CryptoConfigurationId> {
    List<CryptoConfiguration> findByCryptoConfigurationIdClientName(String clientName);
    CryptoConfiguration findByCryptoConfigurationId(CryptoConfigurationId cryptoId);
}
