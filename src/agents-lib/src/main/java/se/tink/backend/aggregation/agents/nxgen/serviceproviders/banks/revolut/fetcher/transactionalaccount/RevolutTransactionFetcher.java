package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class RevolutTransactionFetcher implements TransactionKeyPaginator<TransactionalAccount, String> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(TransactionalAccount account, String key) {
        int count = RevolutConstants.Pagination.COUNT;
        String toDateMillis = (key != null ? key : Long.toString(System.currentTimeMillis()));

        Collection<TransactionEntity> transactionEntities = apiClient.fetchTransactions(count, toDateMillis);

        TransactionKeyPaginatorResponseImpl<String> response = new TransactionKeyPaginatorResponseImpl<>();

        response.setTransactions(transactionEntities.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList())
        );

        response.setNext(transactionEntities.stream()
                .map(TransactionEntity::getStartedDate)
                .min(Comparator.comparing(t -> t))
                .map(t -> Long.toString(t))
                .orElse(null)
        );

        return response;
    }
}
