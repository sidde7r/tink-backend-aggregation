package se.tink.backend.common.repository.mysql.aggregation;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public interface ClusterCryptoConfigurationRepository extends JpaRepository<ClusterCryptoConfiguration, CryptoId>,
        ClusterCryptoConfigurationCustom {
}
