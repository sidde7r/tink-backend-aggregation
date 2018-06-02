package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.backend.core.PortfolioHistory;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface PortfolioHistoryRepositoryCustom extends Creatable {
    List<PortfolioHistory> findAllByUserId(UUID userId);
    List<PortfolioHistory> findAllByUserIdAndAccountId(UUID userId, UUID accountId);
    List<PortfolioHistory> findAllByUserIdAndAccountIdAndPortfolioId(UUID userId, UUID accountId, UUID portfolioId);
    void deleteByUserId(UUID userId);
    void deleteByUserIdAndAccountId(UUID userId, UUID accountId);
}
