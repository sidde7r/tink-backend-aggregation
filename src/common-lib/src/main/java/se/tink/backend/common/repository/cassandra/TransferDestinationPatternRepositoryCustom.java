package se.tink.backend.common.repository.cassandra;

import com.google.common.collect.ListMultimap;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.account.TransferDestinationPattern;

import java.util.List;
import java.util.UUID;

public interface TransferDestinationPatternRepositoryCustom extends Creatable {
    void deleteByUserId(String userId);

    void deleteByUserId(UUID userId);

    void deleteByUserIdAndAccountId(String userId, String accountId);

    void deleteByUserIdAndAccountId(UUID userId, UUID accountId);

    ListMultimap<String, TransferDestinationPattern> findAllByUserId(UUID userId);

    List<TransferDestinationPattern> findAllByUserIdAndAccountId(String userId, String accountId);

    List<TransferDestinationPattern> findAllByUserIdAndAccountId(UUID userId, UUID accountId);

}
