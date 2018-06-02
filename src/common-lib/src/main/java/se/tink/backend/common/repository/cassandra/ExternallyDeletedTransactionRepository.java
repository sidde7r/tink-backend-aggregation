package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.ExternallyDeletedTransaction;

@Repository
@Deprecated
public interface ExternallyDeletedTransactionRepository
        extends CassandraRepository<ExternallyDeletedTransaction>, ExternallyDeletedTransactionRepositoryCustom {
}
