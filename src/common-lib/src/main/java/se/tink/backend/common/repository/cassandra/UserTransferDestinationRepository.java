package se.tink.backend.common.repository.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import se.tink.backend.core.account.UserTransferDestination;

public interface UserTransferDestinationRepository
        extends CassandraRepository<UserTransferDestination>, UserTransferDestinationRepositoryCustom {
}
