package se.tink.backend.common.repository.mysql.aggregation.aggregatorconfigurations;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.AggregatorConfigurations;

public interface AggregatorConfigurationsRepository extends JpaRepository<AggregatorConfigurations, String> {
}
