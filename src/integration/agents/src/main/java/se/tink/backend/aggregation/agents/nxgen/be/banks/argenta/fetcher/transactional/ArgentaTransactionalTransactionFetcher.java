package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class ArgentaTransactionalTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount> {
    private ArgentaApiClient apiClient;
    private final String deviceId;

    public ArgentaTransactionalTransactionFetcher(ArgentaApiClient apiClient, String deviceId) {
        this.apiClient = apiClient;
        this.deviceId = deviceId;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        String accountId = account.getAccountNumber();
        return apiClient.fetchTransactions(accountId, page, deviceId);
    }
}
