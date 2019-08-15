package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TargobankTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private TargobankApiClient apiClient;

    public TargobankTransactionFetcher(TargobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return apiClient.fetchTransactions(account.getAccountNumber());
    }
}
