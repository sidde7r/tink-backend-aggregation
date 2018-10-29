package se.tink.backend.common.repository.mysql.aggregation.cryptoconfigurations;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.CryptoConfigurations;

public interface CryptoConfigurationsRepository extends JpaRepository<CryptoConfigurations, String> {
}
