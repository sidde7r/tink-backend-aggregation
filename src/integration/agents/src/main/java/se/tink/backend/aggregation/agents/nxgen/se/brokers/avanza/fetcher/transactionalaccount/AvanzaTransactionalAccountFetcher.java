package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AvanzaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {
    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;
    private final TemporaryStorage temporaryStorage;

    public AvanzaTransactionalAccountFetcher(
            AvanzaApiClient apiClient,
            AvanzaAuthSessionStorage authSessionStorage,
            TemporaryStorage temporaryStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String holderName = temporaryStorage.getOrDefault(StorageKeys.HOLDER_NAME, null);

        return authSessionStorage.keySet().stream()
                .flatMap(getAccounts(holderName))
                .collect(Collectors.toList());
    }

    private Function<String, Stream<? extends TransactionalAccount>> getAccounts(
            String holderName) {
        return authSession ->
                apiClient.fetchAccounts(authSession).getAccounts().stream()
                        .filter(AccountEntity::isTransactionalAccount)
                        .map(AccountEntity::getAccountId)
                        .map(accId -> apiClient.fetchAccountDetails(accId, authSession))
                        .map(account -> account.toTinkAccount(holderName))
                        .filter(Optional::isPresent)
                        .map(Optional::get);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
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
