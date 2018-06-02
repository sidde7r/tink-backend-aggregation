package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.PortfolioHistory;

public interface PortfolioHistoryRepository extends CassandraRepository<PortfolioHistory>,
        PortfolioHistoryRepositoryCustom {
}
