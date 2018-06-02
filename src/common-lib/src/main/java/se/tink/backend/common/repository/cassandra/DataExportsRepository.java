package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.DataExport;

public interface DataExportsRepository extends CassandraRepository<DataExport>, DataExportsRepositoryCustom {
}
