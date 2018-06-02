package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.transfer.Transfer;


public interface TransferRepository extends CassandraRepository<Transfer>, TransferRepositoryCustom {

}
