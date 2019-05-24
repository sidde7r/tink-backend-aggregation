package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SBABTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final SBABApiClient apiClient;

    public SBABTransactionalAccountTransactionFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date startDate, Date endDate) {
        return apiClient.fetchTransactions(account, startDate, endDate);
    }
}
