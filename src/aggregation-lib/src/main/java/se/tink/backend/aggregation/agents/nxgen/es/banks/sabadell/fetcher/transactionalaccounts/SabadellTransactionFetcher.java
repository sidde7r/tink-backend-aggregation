package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class SabadellTransactionFetcher implements TransactionKeyPaginator<TransactionalAccount, Boolean> {
    private final SabadellApiClient apiClient;

    public SabadellTransactionFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<Boolean> getTransactionsFor(TransactionalAccount account, Boolean key) {
        boolean moreRequest = key != null;
        AccountEntity accountEntity = account.getTemporaryStorage(account.getAccountNumber(), AccountEntity.class);

        return apiClient.fetchTransactions(accountEntity, moreRequest);
    }
}
