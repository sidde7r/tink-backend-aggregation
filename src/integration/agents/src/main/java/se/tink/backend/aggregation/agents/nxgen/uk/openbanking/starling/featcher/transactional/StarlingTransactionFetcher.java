package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Date;

public class StarlingTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final StarlingApiClient apiClient;

    public StarlingTransactionFetcher(StarlingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactions(fromDate, toDate).getTransactionList();
    }
}
