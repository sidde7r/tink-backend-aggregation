package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class SparebankTransactionFetcher implements TransactionIndexPaginator {
    private SparebankApiClient apiClient;

    public SparebankTransactionFetcher(final SparebankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            Account account, int numberOfTransactions, int startIndex) {
        return apiClient.fetchTransactions(
                account.getApiIdentifier(), Integer.toString(startIndex), numberOfTransactions);
    }
}
