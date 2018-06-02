package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.LoanDetails;

public interface LoanDetailsRepository extends CassandraRepository<LoanDetails>, LoanDetailsRepositoryCustom {
}
