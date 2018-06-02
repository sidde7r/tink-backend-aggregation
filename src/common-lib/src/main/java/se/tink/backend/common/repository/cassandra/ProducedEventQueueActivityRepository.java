package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.ProducedEventQueueActivity;

@Repository
public interface ProducedEventQueueActivityRepository
    extends CassandraRepository<ProducedEventQueueActivity>, ProducedEventQueueActivityRepositoryCustom {
}
