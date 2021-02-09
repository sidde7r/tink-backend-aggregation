package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class Sparebank1TransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1TransactionFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
