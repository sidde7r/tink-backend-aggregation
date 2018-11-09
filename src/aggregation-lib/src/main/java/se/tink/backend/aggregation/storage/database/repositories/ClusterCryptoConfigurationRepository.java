package se.tink.backend.aggregation.storage.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public interface ClusterCryptoConfigurationRepository extends JpaRepository<ClusterCryptoConfiguration, CryptoId>,
        ClusterCryptoConfigurationCustom {
}
