package se.tink.backend.common.repository.cassandra;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.CassandraTransactionDeleted;

import java.util.List;

public interface CassandraTransactionDeletedRepositoryCustom extends Creatable {
    Iterable<CassandraTransactionDeleted> saveInBatches(Iterable<CassandraTransactionDeleted> entities);
    Iterable<CassandraTransactionDeleted>  findByUserIdAndIds(String userId, List<String> Ids);
    void deleteByUserId(String userId);
}
