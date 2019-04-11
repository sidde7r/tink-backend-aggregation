package se.tink.backend.aggregation.storage.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

public interface ClusterConfigurationsRepository
        extends JpaRepository<ClusterConfiguration, String> {}
