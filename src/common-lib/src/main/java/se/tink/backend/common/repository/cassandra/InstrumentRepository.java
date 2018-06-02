package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.Instrument;

public interface InstrumentRepository extends CassandraRepository<Instrument>, InstrumentRepositoryCustom {
}
