package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FidorTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private FidorApiClient apiClient;

    public FidorTransactionFetcher(FidorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return apiClient.fetchTransactions(account, page);
    }
}
