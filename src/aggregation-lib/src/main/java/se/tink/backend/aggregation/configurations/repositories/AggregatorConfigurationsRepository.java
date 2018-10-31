package se.tink.backend.aggregation.configurations.repositories.aggregatorconfig;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.aggregation.configurations.models.AggregatorConfiguration;

public interface AggregatorConfigurationsRepository extends JpaRepository<AggregatorConfiguration, String> {
}
