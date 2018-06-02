package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import se.tink.backend.core.Instrument;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface InstrumentRepositoryCustom extends Creatable {
    List<Instrument> findAllByUserId(UUID userId);
    List<Instrument> findAllByUserIdAndPortfolioId(UUID userId, UUID portfolioId);
    public Instrument findOneByUserIdAndPortfolioIdAndInstrumentId(UUID userId, UUID portfolioId, UUID instrumentId);
    void deleteByUserId(UUID userId);
    void deleteByUserIdAndPortfolioId(UUID userId, UUID portfolioId);
    void deleteByUserIdAndPortfolioIdAndId(UUID userId, UUID portfolioId, UUID id);
}
