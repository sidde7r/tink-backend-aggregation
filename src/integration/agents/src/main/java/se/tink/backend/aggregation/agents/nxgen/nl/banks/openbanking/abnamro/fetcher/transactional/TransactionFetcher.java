package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc.TransactionalTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final AbnAmroApiClient apiClient;

    public TransactionFetcher(final AbnAmroApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account, final int page) {
        final TransactionalTransactionsResponse resp = apiClient.getTransactions(account, page);
        resp.setCurrentPage(page);
        return resp;
    }
}