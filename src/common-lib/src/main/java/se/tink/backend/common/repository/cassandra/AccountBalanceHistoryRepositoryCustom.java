package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.backend.core.AccountBalance;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface AccountBalanceHistoryRepositoryCustom extends Creatable {
    void truncate();

    List<AccountBalance> findByUserId(String userId);

    List<AccountBalance> findByUserIdAndAccountId(String userId, String accountId);

    void deleteByUserId(String userId);

    void deleteByUserIdAndAccountId(String userId, String accountId);

    void deleteByUserIdAndAccountIdAndDate(UUID userId, UUID accountId, int date);
}
