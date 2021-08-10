package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TriodosTransactionFetcher extends BerlinGroupTransactionFetcher {

    private TriodosApiClient triodosApiClient;

    public TriodosTransactionFetcher(TriodosApiClient apiClient) {
        super(apiClient);
        this.triodosApiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {
        String url =
                Strings.isNullOrEmpty(nextUrl)
                        ? account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL)
                        : nextUrl;
        return triodosApiClient.fetchTransactionsTriodos(url);
    }
}
