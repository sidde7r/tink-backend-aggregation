package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.InstrumentHistory;

public interface InstrumentHistoryRepository extends CassandraRepository<InstrumentHistory>, InstrumentHistoryRepositoryCustom {
}
