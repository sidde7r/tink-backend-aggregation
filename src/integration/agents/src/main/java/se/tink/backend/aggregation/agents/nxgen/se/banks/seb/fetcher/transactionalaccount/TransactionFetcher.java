package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount;

import com.google.api.client.util.Lists;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SebConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionPageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, TransactionPageKey> {
    private final SebApiClient apiClient;

    public TransactionFetcher(SebApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<TransactionPageKey> getTransactionsFor(
            TransactionalAccount account, TransactionPageKey key) {
        final Response response = apiClient.fetchTransactions(getTransactionQuery(account, key));
        final TransactionPageKey nextKey = getNextKey(response);
        final List<Transaction> tinkTransactions = Lists.newArrayList();

        // When fetching first page, prepend the reserved transactions
        if (Objects.isNull(key)) {
            tinkTransactions.addAll(fetchReservedTransactionsFor(account));
        }

        final List<TransactionEntity> transactions =
                removeOverlappingTransactions(response.getTransactions(), key);
        tinkTransactions.addAll(
                transactions.stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList()));
        return new TransactionKeyPaginatorResponseImpl<>(tinkTransactions, nextKey);
    }

    private TransactionQuery getTransactionQuery(
            TransactionalAccount account, TransactionPageKey key) {
        if (Objects.isNull(key)) {
            // first page
            final String customerId =
                    account.getFromTemporaryStorage(StorageKeys.ACCOUNT_CUSTOMER_ID);
            final String accountId = account.getApiIdentifier();
            return new TransactionQuery(customerId, accountId, 30);
        } else {
            return key.getQuery();
        }
    }

    private TransactionPageKey getNextKey(Response response) {
        final TransactionQuery responseQuery = response.getTransactionQuery();
        final List<TransactionEntity> transactions = response.getTransactions();
        if (transactions.size() < responseQuery.getMaxRows()) {
            return null;
        }

        final TransactionEntity lastTransaction = Iterables.getLast(transactions);
        return new TransactionPageKey(
                responseQuery.getCustomerNumber(),
                responseQuery.getAccountNumber(),
                lastTransaction);
    }

    // The transactions from a page might overlap the ones from the previous page.
    private List<TransactionEntity> removeOverlappingTransactions(
            List<TransactionEntity> transactions, TransactionPageKey key) {
        if (Objects.isNull(key)) {
            return transactions;
        }

        final TransactionEntity lastTransaction = key.getLastTransaction();
        final int index = transactions.indexOf(lastTransaction);
        if (index == -1) {
            return transactions;
        }

        return transactions.subList(index + 1, transactions.size());
    }

    private Collection<Transaction> fetchReservedTransactionsFor(TransactionalAccount account) {
        final String customerId = account.getFromTemporaryStorage(StorageKeys.ACCOUNT_CUSTOMER_ID);
        final String accountId = account.getApiIdentifier();
        final ReservedTransactionQuery query =
                new ReservedTransactionQuery(customerId, accountId, 1);

        final Response response = apiClient.fetchReservedTransactions(query);
        final List<ReservedTransactionEntity> reservedTransactions =
                response.getReservedTransactions();
        if (Objects.isNull(reservedTransactions)) {
            return Collections.emptyList();
        }

        return reservedTransactions.stream()
                .map(ReservedTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
