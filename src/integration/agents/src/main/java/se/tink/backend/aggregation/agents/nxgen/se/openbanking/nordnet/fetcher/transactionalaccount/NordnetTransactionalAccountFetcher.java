package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount;

import java.util.Collection;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
public class NordnetTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, URL> {
    private final NordnetApiClient apiClient;

    public NordnetTransactionalAccountFetcher(NordnetApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        apiClient.getAccounts().forEach(acc -> apiClient.getAccount(acc.getAccountNumber()));
        return null;
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(TransactionalAccount account, URL nextUrl) {
        throw new NotImplementedException("getTransactionsFor not implemented");
    }
}
