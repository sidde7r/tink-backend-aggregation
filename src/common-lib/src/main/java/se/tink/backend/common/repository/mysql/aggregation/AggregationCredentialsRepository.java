package se.tink.backend.common.repository.mysql.aggregation;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.AggregationCredentials;

public interface AggregationCredentialsRepository extends JpaRepository<AggregationCredentials, String> {
}
