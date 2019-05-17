package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BerlinGroupTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    protected final BerlinGroupApiClient apiClient;

    public BerlinGroupTransactionFetcher(BerlinGroupApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {
        String url =
                Strings.isNullOrEmpty(nextUrl)
                        ? account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL)
                        : nextUrl;
        return apiClient.fetchTransactions(url);
    }
}
