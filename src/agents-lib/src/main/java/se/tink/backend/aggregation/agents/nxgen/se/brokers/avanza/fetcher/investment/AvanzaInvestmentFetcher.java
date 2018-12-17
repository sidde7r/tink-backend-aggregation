package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.IsinMap;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentAccountPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.backend.system.rpc.Instrument;
import se.tink.libraries.pair.Pair;

public class AvanzaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final AvanzaApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaInvestmentFetcher(
            AvanzaApiClient apiClient,
            SessionStorage sessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final HolderName holderName =
                new HolderName(temporaryStorage.get(AvanzaConstants.StorageKey.HOLDER_NAME));

        final Supplier<Stream<Pair<String, String>>> sessionAccountPairStream =
                () ->
                        sessionStorage
                                .keySet()
                                .stream()
                                .flatMap(
                                        session ->
                                                apiClient
                                                        .fetchAccounts(session)
                                                        .getAccounts()
                                                        .stream()
                                                        .filter(AccountEntity::isInvestmentAccount)
                                                        .map(
                                                                account ->
                                                                        new Pair<>(
                                                                                session,
                                                                                account
                                                                                        .getAccountId())));
        final Supplier<Stream<Pair<InvestmentAccountPortfolioResponse, IsinMap>>>
                portfolioIsinPairStream =
                        () ->
                                sessionAccountPairStream
                                        .get()
                                        .map(
                                                sessionAccount -> {
                                                    final String session = sessionAccount.first;
                                                    final String account = sessionAccount.second;
                                                    final String date =
                                                            LocalDate.now()
                                                                    .format(
                                                                            DateTimeFormatter
                                                                                    .ISO_DATE);

                                                    return new Pair<>(
                                                            apiClient
                                                                    .fetchInvestmentAccountPortfolio(
                                                                            account, session),
                                                            apiClient
                                                                    .fetchInvestmentTransactions(
                                                                            account, date, session)
                                                                    .toIsinMap());
                                                });

        return sessionAccountPairStream
                .get()
                .flatMap(
                        sessionAccount ->
                                portfolioIsinPairStream
                                        .get()
                                        .map(
                                                portfolioIsinPair -> {
                                                    final String session = sessionAccount.first;
                                                    final InvestmentAccountPortfolioResponse
                                                            portfolio = portfolioIsinPair.first;
                                                    final IsinMap isinMap =
                                                            portfolioIsinPair.second;
                                                    final List<Instrument> instruments =
                                                            portfolio.toTinkInstruments(
                                                                    isinMap, apiClient, session);

                                                    return portfolio.toTinkInvestmentAccount(
                                                            holderName,
                                                            portfolio.toTinkPortfolio(instruments));
                                                }))
                .collect(Collectors.toList());
    }
}
