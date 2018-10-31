package se.tink.backend.aggregation.configurations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.configurations.models.ClusterConfiguration;

public interface ClusterConfigurationsRepository extends JpaRepository<ClusterConfiguration, String> {
}
