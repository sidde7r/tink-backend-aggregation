package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;

import se.tink.backend.core.CassandraTransactionDeleted;

public interface CassandraTransactionDeletedRepository extends CassandraRepository<CassandraTransactionDeleted>,
        CassandraTransactionDeletedRepositoryCustom {
}
