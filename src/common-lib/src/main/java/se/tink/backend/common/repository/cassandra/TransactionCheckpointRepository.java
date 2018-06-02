package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.CheckpointTransaction;

public interface TransactionCheckpointRepository
        extends CassandraRepository<CheckpointTransaction>, TransactionCheckpointRepositoryCustom {
}
