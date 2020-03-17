package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LansforsakringarTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount> {

    private final LansforsakringarApiClient apiClient;

    public LansforsakringarTransactionFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return apiClient.fetchBookedTransactions(account.getApiIdentifier(), page);
    }
}
