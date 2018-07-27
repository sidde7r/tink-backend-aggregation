package se.tink.backend.common.repository.mysql.aggregation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterHostConfiguration;

public interface ClusterHostConfigurationRepository extends JpaRepository<ClusterHostConfiguration, String> {
}
