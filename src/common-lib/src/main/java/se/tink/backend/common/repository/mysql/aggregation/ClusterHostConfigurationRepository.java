package se.tink.backend.common.repository.mysql.aggregation;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterHostConfiguration;

public interface ClusterHostConfigurationRepository extends JpaRepository<ClusterHostConfiguration, String> {
}
