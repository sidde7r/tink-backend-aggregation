package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BbvaTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {
    private final BbvaApiClient apiClient;

    public BbvaTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return apiClient.fetchTransactions(account, page);
    }
}
