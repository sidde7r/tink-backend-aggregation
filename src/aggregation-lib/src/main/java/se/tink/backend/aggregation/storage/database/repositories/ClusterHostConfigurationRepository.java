package se.tink.backend.aggregation.storage.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterHostConfiguration;

public interface ClusterHostConfigurationRepository extends JpaRepository<ClusterHostConfiguration, String> {
}
