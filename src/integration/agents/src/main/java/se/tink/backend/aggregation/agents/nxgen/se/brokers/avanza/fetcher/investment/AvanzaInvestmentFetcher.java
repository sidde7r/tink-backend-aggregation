package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.IsinMap;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.SessionAccountPair;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.MarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AvanzaInvestmentFetcher
        implements AccountFetcher<InvestmentAccount>, TransactionDatePaginator<InvestmentAccount> {
    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;
    private final LocalDateTimeSource localDateTimeSource;

    public AvanzaInvestmentFetcher(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage,
            LocalDateTimeSource localDateTimeSource) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final List<SessionAccountPair> sessionAccountPairs =
                authSessionStorage.keySet().stream()
                        .flatMap(getSessionAccountPairs())
                        .collect(Collectors.toList());

        return getInvestmentAccounts(
                new HolderName(temporaryStorage.get(StorageKeys.HOLDER_NAME)), sessionAccountPairs);
    }

    private Function<String, Stream<? extends SessionAccountPair>> getSessionAccountPairs() {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isInvestmentAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accountId -> new SessionAccountPair(authSession, accountId));
    }

    private Set<InvestmentAccount> getInvestmentAccounts(
            HolderName holder, List<SessionAccountPair> sessionAccountPairs) {
        return sessionAccountPairs.stream()
                .map(sessionAccountPair -> aggregatePortfolioIsinPair(holder, sessionAccountPair))
                .collect(Collectors.toSet());
    }

    private InvestmentAccount aggregatePortfolioIsinPair(
            HolderName holder, SessionAccountPair sessionAccount) {
        final String authSession = sessionAccount.getAuthSession();
        final String account = sessionAccount.getAccountId();
        final String date = localDateTimeSource.now().toLocalDate().toString();

        PortfolioEntity portfolio =
                apiClient.fetchInvestmentAccountPortfolio(account, authSession).getPortfolio();
        IsinMap isinMap =
                apiClient.fetchInvestmentTransactions(account, date, authSession).toIsinMap();

        return aggregateInvestmentAccount(holder, sessionAccount, portfolio, isinMap);
    }

    private InvestmentAccount aggregateInvestmentAccount(
            HolderName holder,
            SessionAccountPair sessionAccount,
            PortfolioEntity portfolio,
            IsinMap isinMap) {
        final String authSession = sessionAccount.getAuthSession();

        final List<Instrument> instruments =
                portfolio.getInstruments().stream()
                        .flatMap(getInstruments(authSession, isinMap))
                        .collect(Collectors.toList());

        AccountDetailsResponse accountDetails =
                apiClient.fetchAccountDetails(sessionAccount.getAccountId(), authSession);

        return portfolio.toTinkInvestmentAccount(
                holder, accountDetails.getClearingNumber(), instruments);
    }

    private Function<InstrumentEntity, Stream<? extends Instrument>> getInstruments(
            String authSession, IsinMap isinMap) {
        return instrument ->
                instrument.getPositions().stream()
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
            final MarketInfoResponse marketInfo =
                    apiClient.getInstrumentMarketInfo(type, orderbookId, authSession);
            final String marketPlace;
            final String isin;
            if (marketInfo != null) {
                marketPlace = marketInfo.getMarketPlace();
                final String marketInfoIsin = marketInfo.getIsin();
                final String mapIsin = isinMap.get(position.getName());
                isin = Optional.ofNullable(marketInfoIsin).orElse(mapIsin);
            } else {
                marketPlace = null;
                isin = isinMap.get(position.getName());
            }

            return position.toTinkInstrument(instrument, marketPlace, isin);
        };
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            InvestmentAccount account, Date fromDate, Date toDate) {
        final String accId = account.getApiIdentifier();
        final String fromDateStr = ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate);
        final String toDateStr = ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate);

        List<Transaction> transactions =
                authSessionStorage.keySet().stream()
                        .filter(
                                authSession ->
                                        apiClient.authSessionHasAccountId(authSession, accId))
                        .findFirst()
                        .map(
                                authSession ->
                                        apiClient.fetchTransactions(
                                                accId, fromDateStr, toDateStr, authSession))
                        .map(TransactionsResponse::getTransactions)
                        .orElse(new ArrayList<>());

        return PaginatorResponseImpl.create(transactions);
    }
}
