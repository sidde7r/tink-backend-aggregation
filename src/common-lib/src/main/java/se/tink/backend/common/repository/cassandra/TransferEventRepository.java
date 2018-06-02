package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.transfer.TransferEvent;

public interface TransferEventRepository extends CassandraRepository<TransferEvent>, TransferEventRepositoryCustom {
    
}
