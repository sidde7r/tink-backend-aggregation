package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.account.TransferDestinationPattern;

public interface TransferDestinationPatternRepository extends CassandraRepository<TransferDestinationPattern>, TransferDestinationPatternRepositoryCustom {

}