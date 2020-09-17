package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class NordeaTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {
    private final NordeaSEApiClient apiClient;

    public NordeaTransactionFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchAccountTransactions(account, fromDate, toDate);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        return Collections.emptyList();
    }
}
