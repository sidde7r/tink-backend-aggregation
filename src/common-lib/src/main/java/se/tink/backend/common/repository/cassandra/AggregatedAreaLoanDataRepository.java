package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.interests.AggregatedAreaLoanData;

public interface AggregatedAreaLoanDataRepository extends CassandraRepository<AggregatedAreaLoanData>, AggregatedAreaLoanDataRepositoryCustom {

}
