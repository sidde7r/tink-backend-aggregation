package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.main.mappers.CoreInstrumentMapper;
import se.tink.backend.main.mappers.CorePortfolioMapper;
import se.tink.backend.rpc.Instrument;
import se.tink.backend.rpc.Portfolio;
import se.tink.libraries.uuid.UUIDUtils;

public class InvestmentServiceController {

    private PortfolioRepository portfolioRepository;
    private InstrumentRepository instrumentRepository;

    @Inject InvestmentServiceController(PortfolioRepository portfolioRepository,
            InstrumentRepository instrumentRepository) {
        this.portfolioRepository = portfolioRepository;
        this.instrumentRepository = instrumentRepository;
    }

    public List<Portfolio> getPortfolios(String userId) {
        List<se.tink.backend.core.Portfolio> allPortfolios = portfolioRepository
                .findAllByUserId(UUIDUtils.fromTinkUUID(userId));

        if (allPortfolios == null || allPortfolios.isEmpty()) {
            return Lists.newArrayList();
        }

        List<se.tink.backend.core.Instrument> allInstruments = instrumentRepository
                .findAllByUserId(UUIDUtils.fromTinkUUID(userId));

        if (allInstruments == null || allInstruments.isEmpty()) {
            allPortfolios.forEach(p -> p.setInstruments(Lists.newArrayList()));
            return allPortfolios.stream().map(CorePortfolioMapper::fromCoreToMain).collect(Collectors.toList());
        }

        Map<UUID, Portfolio> portfoliosById = allPortfolios.stream()
                .map(CorePortfolioMapper::fromCoreToMain)
                .collect(Collectors.toMap(Portfolio::getId, p -> p));

        Map<UUID, List<Instrument>> instrumentsByPortfolioId = allInstruments.stream()
                .map(CoreInstrumentMapper::fromCoreToMain)
                .collect(Collectors.groupingBy(Instrument::getPortfolioId));

        for (UUID portfolioId : instrumentsByPortfolioId.keySet()) {
            if (!portfoliosById.containsKey(portfolioId)) {
                continue;
            }

            List<Instrument> instruments = instrumentsByPortfolioId.get(portfolioId);
            portfoliosById.get(portfolioId).setInstruments(instruments);
        }

        return Lists.newArrayList(portfoliosById.values());
    }
}
