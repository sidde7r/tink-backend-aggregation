package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngAtTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final IngAtApiClient apiClient;
    private final IngAtSessionStorage sessionStorage;

    public IngAtTransactionFetcher(IngAtApiClient apiClient, IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactionsResponse(account, fromDate, toDate);
    }
}
