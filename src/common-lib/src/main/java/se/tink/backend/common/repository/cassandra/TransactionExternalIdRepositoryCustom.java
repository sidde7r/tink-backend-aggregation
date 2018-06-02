package se.tink.backend.common.repository.cassandra;

import java.util.List;
import se.tink.backend.core.TransactionExternalId;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface TransactionExternalIdRepositoryCustom extends Creatable {
    TransactionExternalId findByAccountIdUserIdAndExternalTransactionId(String accountId, String userId,
            String externalTransactionId);

    List<TransactionExternalId> findAllByAccountIdUserIdAndExternalTransactionIds(String accountId,
            String userId, List<String> externalTransactionIds);
}
