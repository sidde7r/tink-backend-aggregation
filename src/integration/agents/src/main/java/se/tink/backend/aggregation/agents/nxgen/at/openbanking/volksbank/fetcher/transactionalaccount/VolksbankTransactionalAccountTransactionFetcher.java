package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class VolksbankTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final VolksbankApiClient apiClient;

    public VolksbankTransactionalAccountTransactionFetcher(VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactionsFor(account, fromDate, toDate);
    }
}
