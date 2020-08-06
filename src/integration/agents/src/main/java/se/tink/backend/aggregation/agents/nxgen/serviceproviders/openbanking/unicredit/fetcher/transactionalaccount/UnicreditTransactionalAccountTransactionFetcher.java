package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UnicreditTransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, URL> {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditTransactionalAccountTransactionFetcher(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL url) {
        if (url == null) {
            return apiClient.getTransactionsFor(account);
        } else {
            return apiClient.getTransactionsForNextUrl(url);
        }
    }
}
