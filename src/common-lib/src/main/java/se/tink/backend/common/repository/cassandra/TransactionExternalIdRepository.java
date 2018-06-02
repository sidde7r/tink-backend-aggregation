package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.TransactionExternalId;

@Repository
public interface TransactionExternalIdRepository
    extends CassandraRepository<TransactionExternalId>, TransactionExternalIdRepositoryCustom {
}
