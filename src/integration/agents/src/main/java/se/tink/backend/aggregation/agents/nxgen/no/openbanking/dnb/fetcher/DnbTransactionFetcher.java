package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DnbTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final DnbApiClient apiClient;

    public DnbTransactionFetcher(final DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            final TransactionalAccount account, final String nextUrl) {
        return apiClient.fetchTransactions(account.getApiIdentifier());
    }
}
