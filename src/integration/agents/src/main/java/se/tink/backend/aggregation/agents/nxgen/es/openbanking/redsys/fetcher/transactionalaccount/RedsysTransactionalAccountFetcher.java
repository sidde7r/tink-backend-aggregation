package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.URL;

public class RedsysTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, URL> {

    private final RedsysApiClient apiClient;

    public RedsysTransactionalAccountFetcher(RedsysApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Collections.emptyList();
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextUrl) {
        throw new NotImplementedException("getTransactionsFor not implemented");
    }
}
