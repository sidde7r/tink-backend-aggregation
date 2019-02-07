package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.IsinMap;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PortfolioIsinPair;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.SessionAccountPair;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.backend.aggregation.agents.models.Instrument;

public class AvanzaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaInvestmentFetcher(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final HolderName holder = new HolderName(temporaryStorage.get(StorageKeys.HOLDER_NAME));

        final List<SessionAccountPair> sessionAccountPairs =
                authSessionStorage
                        .keySet()
                        .stream()
                        .flatMap(getSessionAccountPairs())
                        .collect(Collectors.toList());

        return sessionAccountPairs
                .stream()
                .flatMap(getInvestmentAccounts(holder, sessionAccountPairs))
                .collect(Collectors.toList());
    }

    private Function<String, Stream<? extends SessionAccountPair>> getSessionAccountPairs() {
        return authSession ->
                apiClient
                        .fetchAccounts(authSession)
                        .getAccounts()
                        .stream()
                        .filter(AccountEntity::isInvestmentAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accountId -> new SessionAccountPair(authSession, accountId));
    }

    private Function<SessionAccountPair, Stream<? extends InvestmentAccount>> getInvestmentAccounts(
            HolderName holder, List<SessionAccountPair> sessionAccountPairs) {
        return sessionAccount ->
                sessionAccountPairs
                        .stream()
                        .map(aggregatePortfolioIsinPair())
                        .map(aggregateInvestmentAccount(holder, sessionAccount));
    }

    private Function<SessionAccountPair, PortfolioIsinPair> aggregatePortfolioIsinPair() {
        return sessionAccount -> {
            final String authSession = sessionAccount.getAuthSession();
            final String account = sessionAccount.getAccountId();
            final String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

            return new PortfolioIsinPair(
                    apiClient.fetchInvestmentAccountPortfolio(account, authSession).getPortfolio(),
                    apiClient.fetchInvestmentTransactions(account, date, authSession).toIsinMap());
        };
    }

    private Function<PortfolioIsinPair, InvestmentAccount> aggregateInvestmentAccount(
            HolderName holder, SessionAccountPair sessionAccount) {
        return portfolioIsinPair -> {
            final String authSession = sessionAccount.getAuthSession();
            final PortfolioEntity portfolio = portfolioIsinPair.getPortfolio();
            final IsinMap isinMap = portfolioIsinPair.getIsinMap();

            final List<Instrument> instruments =
                    portfolio
                            .getInstruments()
                            .stream()
                            .flatMap(getInstruments(authSession, isinMap))
                            .collect(Collectors.toList());

            return portfolio.toTinkInvestmentAccount(holder, instruments);
        };
    }

    private Function<InstrumentEntity, Stream<? extends Instrument>> getInstruments(
            String authSession, IsinMap isinMap) {
        return instrument ->
                instrument
                        .getPositions()
                        .stream()
                        .map(aggregateInstrument(isinMap, apiClient, authSession, instrument));
    }

    private Function<PositionEntity, Instrument> aggregateInstrument(
            IsinMap isinMap,
            AvanzaApiClient apiClient,
            String authSession,
            InstrumentEntity instrument) {

        return position -> {
            final String type = instrument.getInstrumentType();
            final String orderbookId = position.getOrderbookId();
            final String market = apiClient.getInstrumentMarket(type, orderbookId, authSession);

            return position.toTinkInstrument(instrument, market, isinMap);
        };
    }
}
