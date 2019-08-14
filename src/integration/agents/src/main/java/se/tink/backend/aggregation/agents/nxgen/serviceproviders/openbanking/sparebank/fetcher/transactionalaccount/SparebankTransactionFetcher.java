package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SparebankTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private SparebankApiClient apiClient;

    public SparebankTransactionFetcher(final SparebankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
