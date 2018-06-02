package se.tink.backend.common.repository.cassandra;

import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.ExternallyDeletedTransaction;

@Deprecated
public interface ExternallyDeletedTransactionRepositoryCustom extends Creatable {
    ExternallyDeletedTransaction findByAccountIdUserIdAndExternalTransactionId(String accountId, String userId,
            String externalTransactionId);
}
