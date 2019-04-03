package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class RaiffeisenTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final RaiffeisenApiClient apiClient;

    public RaiffeisenTransactionalAccountTransactionFetcher(RaiffeisenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactions(account, fromDate, toDate);
    }
}
