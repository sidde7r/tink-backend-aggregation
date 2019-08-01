package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.PendingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.PendingTransactionQuery;
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
    private final SEBApiClient apiClient;

    public TransactionFetcher(SEBApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<TransactionPageKey> getTransactionsFor(
            TransactionalAccount account, TransactionPageKey key) {
        final Response response = apiClient.fetchTransactions(getTransactionQuery(account, key));
        final TransactionPageKey nextKey = getNextKey(response);
        final List<TransactionEntity> transactions =
                removeOverlappingTransactions(response.getTransactions(), key);

        final List<Transaction> tinkTransactions =
                transactions.stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        if (key == null) {
            tinkTransactions.addAll(fetchPendingTransactionsFor(account));
        }
        return new TransactionKeyPaginatorResponseImpl<>(tinkTransactions, nextKey);
    }

    private TransactionQuery getTransactionQuery(
            TransactionalAccount account, TransactionPageKey key) {
        if (key == null) {
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
        if (key == null) {
            return transactions;
        }

        final TransactionEntity lastTransaction = key.getLastTransaction();
        final int index = transactions.indexOf(lastTransaction);
        if (index == -1) {
            return transactions;
        }

        return transactions.subList(index + 1, transactions.size());
    }

    private Collection<Transaction> fetchPendingTransactionsFor(TransactionalAccount account) {
        final String customerId = account.getFromTemporaryStorage(StorageKeys.ACCOUNT_CUSTOMER_ID);
        final String accountId = account.getApiIdentifier();
        final PendingTransactionQuery query = new PendingTransactionQuery(customerId, accountId, 1);

        final Response response = apiClient.fetchPendingTransactions(query);
        final List<PendingTransactionEntity> pendingTransactions =
                response.getPendingTransactions();
        if (pendingTransactions == null) {
            return Collections.emptyList();
        }

        return pendingTransactions.stream()
                .map(PendingTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
