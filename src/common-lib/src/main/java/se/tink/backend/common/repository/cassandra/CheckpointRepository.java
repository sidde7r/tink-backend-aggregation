package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.Checkpoint;

public interface CheckpointRepository extends CassandraRepository<Checkpoint>, CheckpointRepositoryCustom {
}
