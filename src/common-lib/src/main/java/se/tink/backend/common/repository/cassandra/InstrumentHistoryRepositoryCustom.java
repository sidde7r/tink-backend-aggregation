package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.backend.core.InstrumentHistory;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface InstrumentHistoryRepositoryCustom extends Creatable {
    List<InstrumentHistory> findAllByUserId(UUID userId);
    List<InstrumentHistory> findAllByUserIdAndPortfolioId(UUID userId, UUID portfolioId);
    List<InstrumentHistory> findAllByUserIdAndPortfolioIdAndInstrumentId(UUID userId, UUID portfolioId, UUID instrumentId);
    void deleteByUserId(UUID userId);
    void deleteByUserIdAndPortfolioId(UUID userId, UUID portfolioId);
    void deleteByUserIdAndPortfolioIdAndInstrumentId(UUID userId, UUID portfolioId, UUID id);
}
