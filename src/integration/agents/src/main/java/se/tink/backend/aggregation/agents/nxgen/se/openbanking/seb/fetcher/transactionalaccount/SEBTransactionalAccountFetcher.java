package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SEBApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SEBTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
        TransactionPagePaginator<TransactionalAccount> {
    private final SEBApiClient apiClient;

    public SEBTransactionalAccountFetcher(SEBApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return apiClient.fetchTransactions(account, page);
    }
}
