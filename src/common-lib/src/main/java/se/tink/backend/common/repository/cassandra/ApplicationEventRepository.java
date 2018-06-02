package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.application.ApplicationEvent;

public interface ApplicationEventRepository
        extends CassandraRepository<ApplicationEvent>, ApplicationEventRepositoryCustom {
}
