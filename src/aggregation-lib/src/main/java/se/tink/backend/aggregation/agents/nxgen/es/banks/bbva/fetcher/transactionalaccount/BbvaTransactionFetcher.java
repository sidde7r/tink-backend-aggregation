package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class BbvaTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private BbvaApiClient apiClient;

    public BbvaTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionPagePaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return apiClient.fetchAccountTransactions(account,
                page * BbvaConstants.PAGE_SIZE);
    }
}
