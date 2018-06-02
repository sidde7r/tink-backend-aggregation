package se.tink.backend.common.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.common.repository.cassandra.InstrumentHistoryRepository;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.PortfolioHistoryRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.core.Instrument;
import se.tink.backend.core.InstrumentHistory;
import se.tink.backend.core.Portfolio;
import se.tink.backend.core.PortfolioHistory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

public class InvestmentDao {
    private final static LogUtils log = new LogUtils(InvestmentDao.class);
    private final MetricId INSTRUMENTS_ID = MetricId.newId("investments");
    private final MetricId PORTFOLIOS_ID = MetricId.newId("portfolios");
    private final PortfolioRepository portfolioRepository;
    private final PortfolioHistoryRepository portfolioHistoryRepository;
    private final InstrumentRepository instrumentRepository;
    private final InstrumentHistoryRepository instrumentHistoryRepository;
    private final MetricRegistry metricRegistry;

    @Inject
    public InvestmentDao(PortfolioRepository portfolioRepository, PortfolioHistoryRepository portfolioHistoryRepository,
            InstrumentRepository instrumentRepository, InstrumentHistoryRepository instrumentHistoryRepository,
            MetricRegistry metricRegistry) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioHistoryRepository = portfolioHistoryRepository;
        this.instrumentRepository = instrumentRepository;
        this.instrumentHistoryRepository = instrumentHistoryRepository;
        this.metricRegistry = metricRegistry;
    }

    public void save(Portfolio incomingPortfolio) {
        // Ensure we have set user id and account id
        Preconditions.checkNotNull(incomingPortfolio.getUserId());
        Preconditions.checkNotNull(incomingPortfolio.getAccountId());
        // We always need a unique identifier to match the incoming portfolio with any existing in the database
        Preconditions.checkNotNull(incomingPortfolio.getUniqueIdentifier());

        Date timestamp = new Date(); // Use same timestamp for portfolio and instruments

        // Find the users existing portfolios for the account
        List<Portfolio> existingPortfolios = portfolioRepository
                .findAllByUserIdAndAccountId(incomingPortfolio.getUserId(), incomingPortfolio.getAccountId());

        // If there were no existing portfolios we save a new one and all the instruments
        if (existingPortfolios == null || existingPortfolios.isEmpty()) {
            saveNewPortfolio(incomingPortfolio, timestamp);
            saveNewInstruments(incomingPortfolio.getInstruments(), timestamp);
            return;
        }

        // Check if we can find match the unique identifier for the portfolio with any existing portfolio
        Optional<Portfolio> existingPortfolio = existingPortfolios.stream()
                .filter(p -> Objects.equals(p.getUniqueIdentifier(), incomingPortfolio.getUniqueIdentifier()))
                .findFirst();


        // If we did not find a matching portfolio we save a new one and all instruments
        if (!existingPortfolio.isPresent()) {
            saveNewPortfolio(incomingPortfolio, timestamp);
            saveNewInstruments(incomingPortfolio.getInstruments(), timestamp);
            return;
        }

        incomingPortfolio.setId(existingPortfolio.get().getId());
        savePortfolio(incomingPortfolio, timestamp);

        // Time to save the instruments for the existing portfolio
        List<Instrument> incomingInstrumentsForPortfolio = incomingPortfolio.getInstruments();

        if ((incomingInstrumentsForPortfolio == null || incomingInstrumentsForPortfolio.isEmpty())) {
            return;
        }

        List<Instrument> instrumentWithoutUniqueIdentifier = incomingInstrumentsForPortfolio.stream()
                .filter(instrument -> instrument.getUniqueIdentifier() == null)
                .collect(Collectors.toList());

        if (!instrumentWithoutUniqueIdentifier.isEmpty()) {
            log.warn(incomingPortfolio.getCredentials(),
                    "found unique identifier(s) that was null - removing from incoming instruments");
            incomingInstrumentsForPortfolio.removeAll(instrumentWithoutUniqueIdentifier);
        }

        // Find all the users existing instruments for the specific portfolio
        List<Instrument> existingInstrumentsForPortfolio = instrumentRepository.findAllByUserIdAndPortfolioId(
                incomingPortfolio.getUserId(), incomingPortfolio.getId());

        if (existingInstrumentsForPortfolio == null || existingInstrumentsForPortfolio.isEmpty()) {
            saveNewInstruments(incomingInstrumentsForPortfolio, timestamp);
            return;
        }

        // Save the instruments
        for (Instrument incomingInstrument : incomingInstrumentsForPortfolio) {
            Optional<Instrument> optionalExistingInstrument = existingInstrumentsForPortfolio.stream()
                    .filter(i -> i.getUniqueIdentifier() != null)
                    .filter(i ->
                            Objects.equals(i.getUniqueIdentifier().toLowerCase(),
                                    incomingInstrument.getUniqueIdentifier().toLowerCase()))
                    .findFirst();

            // If the instrument isn't present we save the incoming one
            if (!optionalExistingInstrument.isPresent()) {
                saveNewInstrument(incomingInstrument, timestamp);
                continue;
            }

            Instrument existingInstrument = optionalExistingInstrument.get();

            if (incomingInstrument.getIsin() == null) {
                incomingInstrument.setIsin(existingInstrument.getIsin());
            }

            incomingInstrument.setId(existingInstrument.getId());
            saveInstrument(incomingInstrument, timestamp);
        }

        // Delete existing instrument that were not in the incoming instruments
        Set<Instrument> incomingInstruments = Sets.newHashSet(incomingInstrumentsForPortfolio);
        Set<Instrument> existingInstruments = Sets.newHashSet(existingInstrumentsForPortfolio);

        // Gives the instruments in existing instruments that are not in incoming instruments.
        // Difference ignores the the instruments that are in incoming but not in existing instruments.
        ImmutableSet<Instrument> instrumentsToDelete = Sets.difference(
                existingInstruments, incomingInstruments).immutableCopy();

        existingInstruments.forEach(existingInstrument -> {
            if (instrumentsToDelete.contains(existingInstrument)) {
                deleteInstrument(existingInstrument);
            }
        });
    }

    private void saveNewPortfolio(Portfolio newPortfolio, Date timestamp) {
        newPortfolio.setId(UUID.randomUUID());
        savePortfolio(newPortfolio, timestamp);
    }

    private void savePortfolio(Portfolio portfolio, Date timestamp) {
        UUID portfolioId = portfolio.getId();
        Preconditions.checkNotNull(portfolioId);

        metricRegistry.meter(PORTFOLIOS_ID
                .label("type", portfolio.getType().name())).inc();

        portfolioRepository.save(portfolio);
        portfolioHistoryRepository.save(PortfolioHistory.createFromPortfolioAndTimestamp(portfolio, timestamp));

        portfolio.getInstruments().forEach(i -> i.setPortfolioId(portfolioId));
    }

    private void saveNewInstruments(List<Instrument> newInstruments, Date timestamp) {
        if (newInstruments == null || newInstruments.isEmpty()) {
            return;
        }

        newInstruments.forEach(newInstrument -> saveNewInstrument(newInstrument, timestamp));
    }

    private void saveNewInstrument(Instrument instrument, Date timestamp) {
        instrument.setId(UUID.randomUUID());
        saveInstrument(instrument, timestamp);
    }

    private void saveInstrument(Instrument instrument, Date timestamp) {
        Preconditions.checkNotNull(instrument.getId());
        Preconditions.checkNotNull(instrument.getPortfolioId());
        Preconditions.checkNotNull(instrument.getUserId());

        metricRegistry.meter(INSTRUMENTS_ID
                .label("has_isin", instrument.getIsin() != null)
                .label("action", "save")
                .label("type", instrument.getType().name())).inc();

        instrumentRepository.save(instrument);
        instrumentHistoryRepository.save(InstrumentHistory.createFromInstrumentAndTimestamp(instrument, timestamp));
    }

    private void deleteInstrument(Instrument instrument) {
        Preconditions.checkNotNull(instrument.getId());
        Preconditions.checkNotNull(instrument.getPortfolioId());
        Preconditions.checkNotNull(instrument.getUserId());

        metricRegistry.meter(INSTRUMENTS_ID
                .label("has_isin", instrument.getIsin() != null)
                .label("action", "delete")
                .label("type", instrument.getType().name())).inc();

        instrumentRepository.deleteByUserIdAndPortfolioIdAndId(instrument.getUserId(), instrument.getPortfolioId(),
                instrument.getId());
        instrumentHistoryRepository.deleteByUserIdAndPortfolioIdAndInstrumentId(instrument.getUserId(),
                instrument.getPortfolioId(), instrument.getId());
    }

    public void deleteByUserId(String userId) {
        UUID userIdAsUuid = UUIDUtils.fromTinkUUID(userId);
        portfolioRepository.deleteByUserId(userIdAsUuid);
        portfolioHistoryRepository.deleteByUserId(userIdAsUuid);
        instrumentHistoryRepository.deleteByUserId(userIdAsUuid);
        instrumentRepository.deleteByUserId(userIdAsUuid);
    }

    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        UUID userIdAsUuid = UUIDUtils.fromTinkUUID(userId);
        UUID accountIdAsUuid = UUIDUtils.fromTinkUUID(accountId);

        List<Portfolio> allByUserId = portfolioRepository.findAllByUserIdAndAccountId(userIdAsUuid,
                accountIdAsUuid);

        List<UUID> portfolioIds = allByUserId.stream()
                .map(Portfolio::getId)
                .distinct()
                .collect(Collectors.toList());

        portfolioRepository.deleteByUserIdAndAccountId(userIdAsUuid, accountIdAsUuid);
        portfolioHistoryRepository.deleteByUserIdAndAccountId(userIdAsUuid, accountIdAsUuid);
        for (UUID portfolioId : portfolioIds) {
            instrumentRepository.deleteByUserIdAndPortfolioId(userIdAsUuid, portfolioId);
            instrumentHistoryRepository.deleteByUserIdAndPortfolioId(userIdAsUuid, portfolioId);
        }
    }
}
