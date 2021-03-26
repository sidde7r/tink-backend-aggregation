package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AvanzaInvestmentFetcher
        implements AccountFetcher<InvestmentAccount>, TransactionDatePaginator<InvestmentAccount> {
    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;
    private final LocalDateTimeSource localDateTimeSource;
    private final String clusterId;

    public AvanzaInvestmentFetcher(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage,
            LocalDateTimeSource localDateTimeSource,
            String clusterId) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.localDateTimeSource = localDateTimeSource;
        this.clusterId = clusterId;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final List<SessionAccountPair> sessionAccountPairs =
                authSessionStorage.keySet().stream()
                        .flatMap(getSessionAccountPairs())
                        .collect(Collectors.toList());

        return getInvestmentAccounts(sessionAccountPairs);
    }

    private Function<String, Stream<? extends SessionAccountPair>> getSessionAccountPairs() {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isInvestmentAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accountId -> new SessionAccountPair(authSession, accountId));
    }

    private Set<InvestmentAccount> getInvestmentAccounts(
            List<SessionAccountPair> sessionAccountPairs) {
        return sessionAccountPairs.stream()
                .map(sessionAccountPair -> aggregatePortfolioIsinPair(sessionAccountPair))
                .collect(Collectors.toSet());
    }

    private InvestmentAccount aggregatePortfolioIsinPair(SessionAccountPair sessionAccount) {
        final String authSession = sessionAccount.getAuthSession();
        final String account = sessionAccount.getAccountId();
        final String date = localDateTimeSource.now().toLocalDate().toString();

        PortfolioEntity portfolio =
                apiClient.fetchInvestmentAccountPortfolio(account, authSession).getPortfolio();
        IsinMap isinMap =
                apiClient.fetchInvestmentTransactions(account, date, authSession).toIsinMap();

        return aggregateInvestmentAccount(sessionAccount, portfolio, isinMap);
    }

    private InvestmentAccount aggregateInvestmentAccount(
            SessionAccountPair sessionAccount, PortfolioEntity portfolio, IsinMap isinMap) {
        final String authSession = sessionAccount.getAuthSession();

        final List<InstrumentModule> instruments =
                portfolio.getInstruments().stream()
                        .flatMap(getInstruments(authSession, isinMap))
                        .collect(Collectors.toList());

        String holderName = temporaryStorage.getOrDefault(StorageKeys.HOLDER_NAME, null);

        AccountDetailsResponse accountDetails =
                apiClient.fetchAccountDetails(sessionAccount.getAccountId(), authSession);

        return portfolio.toTinkInvestmentAccount(
                holderName, accountDetails.getClearingNumber(), instruments, clusterId);
    }

    private Function<InstrumentEntity, Stream<? extends InstrumentModule>> getInstruments(
            String authSession, IsinMap isinMap) {
        return instrument ->
                instrument.getPositions().stream()
                        .map(aggregateInstrument(isinMap, apiClient, authSession, instrument))
                        .filter(Objects::nonNull);
    }

    private Function<PositionEntity, InstrumentModule> aggregateInstrument(
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
                return position.toTinkInstrument(instrument, marketPlace, isin);
            }
            return null;
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
