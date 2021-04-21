package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaBaseTransactionalAccountFetcher<R extends GetTransactionsResponse<?>>
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {
    private final NordeaBaseApiClient apiClient;
    private Class<R> responseClass;
    private String providerMarket;

    public NordeaBaseTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient, Class<R> responseClass, String providerMarket) {
        this.apiClient = apiClient;
        this.responseClass = responseClass;
        this.providerMarket = providerMarket;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().toTinkAccounts();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return apiClient
                .getTransactions(account, key, responseClass)
                .setProviderMarket(providerMarket);
    }
}
