package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.DataExportFragment;

public interface DataExportFragmentsRepository
        extends CassandraRepository<DataExportFragment>, DataExportFragmentsRepositoryCustom {
}
