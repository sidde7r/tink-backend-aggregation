package se.tink.backend.common.repository.mysql.aggregation.cryptoconfigurations;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.CryptoConfiguration;

public interface CryptoConfigurationsRepository extends JpaRepository<CryptoConfiguration, String> {
}
