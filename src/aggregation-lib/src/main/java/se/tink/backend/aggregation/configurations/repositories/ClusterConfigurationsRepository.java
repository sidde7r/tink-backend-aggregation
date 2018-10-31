package se.tink.backend.aggregation.configurations.repositories.clusterconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.configurations.models.ClusterConfiguration;

public interface ClusterConfigurationsRepository extends JpaRepository<ClusterConfiguration, String> {
}
