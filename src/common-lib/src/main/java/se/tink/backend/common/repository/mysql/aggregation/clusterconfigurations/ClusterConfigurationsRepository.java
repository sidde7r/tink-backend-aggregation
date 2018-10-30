package se.tink.backend.common.repository.mysql.aggregation.clusterconfigurations;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.ClusterConfiguration;

public interface ClusterConfigurationsRepository extends JpaRepository<ClusterConfiguration, String> {
}
