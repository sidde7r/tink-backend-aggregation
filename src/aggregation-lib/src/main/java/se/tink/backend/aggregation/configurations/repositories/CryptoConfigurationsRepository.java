package se.tink.backend.aggregation.configurations.repositories.cryptoconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;

public interface CryptoConfigurationsRepository extends JpaRepository<CryptoConfiguration, String> {
}
