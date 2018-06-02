package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.application.ApplicationFormEvent;

public interface ApplicationFormEventRepository
        extends CassandraRepository<ApplicationFormEvent>, ApplicationFormEventRepositoryCustom {
}
