package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.backend.core.Portfolio;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface PortfolioRepositoryCustom extends Creatable {
    List<Portfolio> findAllByUserId(UUID userId);
    List<Portfolio> findAllByUserIdAndAccountId(UUID userId, UUID accountId);
    Portfolio findOneByUserIdAndAccountIdAndPortfolioId(UUID userId, UUID accountId, UUID portfolioId);
    void deleteByUserId(UUID userId);
    void deleteByUserIdAndAccountId(UUID userId, UUID accountId);
}
