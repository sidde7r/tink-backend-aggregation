package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.application.ApplicationArchiveRow;

public interface ApplicationArchiveRepository
        extends CassandraRepository<ApplicationArchiveRow>, ApplicationArchiveRepositoryCustom {

}
