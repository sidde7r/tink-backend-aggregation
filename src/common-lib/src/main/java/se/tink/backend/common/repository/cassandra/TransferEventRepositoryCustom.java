package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.transfer.TransferEvent;

public interface TransferEventRepositoryCustom extends Creatable {
    void deleteByUserId(String userId);

    List<TransferEvent> findAllByUserIdAndTransferId(UUID userId, UUID transferId);

    List<TransferEvent> findAllByUserId(UUID userId);
}
