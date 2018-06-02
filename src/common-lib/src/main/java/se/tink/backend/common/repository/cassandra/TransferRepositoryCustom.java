package se.tink.backend.common.repository.cassandra;

import java.util.UUID;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.transfer.Transfer;
import java.util.List;

public interface TransferRepositoryCustom extends Creatable {
    void deleteByUserId(String userId);
    
    void deleteByUserIdAndCredentialsId(String userId, String credentialsId);

    List<Transfer> findAllByUserId(String userId);

    List<Transfer> findAllByUserIdAndCredentialsId(String userId, String credentialsId);

    Transfer findOneByUserIdAndId(String userId, String id);
    
    Transfer findOneByUserIdAndId(UUID userId, UUID id);
}
