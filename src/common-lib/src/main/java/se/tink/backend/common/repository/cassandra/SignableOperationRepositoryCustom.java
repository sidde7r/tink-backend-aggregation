package se.tink.backend.common.repository.cassandra;

import java.util.Optional;
import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.signableoperation.SignableOperation;

import se.tink.backend.core.enums.SignableOperationTypes;
import java.util.List;

public interface SignableOperationRepositoryCustom extends Creatable {
    void deleteByUserId(String userId);

    Optional<SignableOperation> findOneByUserIdAndUnderlyingId(String userId, UUID underlyingId);

    List<SignableOperation> findAllByUserId(String userId);
    
    List<SignableOperation> findAllByUserIdAndType(String userId, SignableOperationTypes type);
}
