package se.tink.backend.common.repository.mysql.aggregation.aggregationcredentials;

import org.springframework.data.jpa.repository.JpaRepository;
import se.tink.backend.core.AggregationCredentials;

public interface AggregationCredentialsRepository extends JpaRepository<AggregationCredentials, String> {
}
