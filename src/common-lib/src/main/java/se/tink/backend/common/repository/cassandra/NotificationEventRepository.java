package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.NotificationEvent;

public interface NotificationEventRepository extends CassandraRepository<NotificationEvent>, NotificationEventRepositoryCustom {

}
