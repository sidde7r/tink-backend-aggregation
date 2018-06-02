package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.account.UserTransferDestination;

public interface UserTransferDestinationRepositoryCustom extends Creatable {
    void deleteByUserId(String userId);

    void deleteByUserId(UUID userId);

    List<UserTransferDestination> findAllByUserId(UUID userId);
}
