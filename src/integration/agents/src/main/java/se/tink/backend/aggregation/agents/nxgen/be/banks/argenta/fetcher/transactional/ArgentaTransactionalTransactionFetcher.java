package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaPersistentStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ArgentaTransactionalTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount> {
    private ArgentaApiClient apiClient;
    private final ArgentaPersistentStorage persistentStorage;

    public ArgentaTransactionalTransactionFetcher(
            ArgentaApiClient apiClient, ArgentaPersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        String accountId = account.getAccountNumber();
        return apiClient.fetchTransactions(accountId, page, persistentStorage.getDeviceId());
    }
}
