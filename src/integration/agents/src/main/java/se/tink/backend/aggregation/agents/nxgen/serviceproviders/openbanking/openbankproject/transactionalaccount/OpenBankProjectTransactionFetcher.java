package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class OpenBankProjectTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount> {

    private final OpenBankProjectApiClient apiClient;

    public OpenBankProjectTransactionFetcher(final OpenBankProjectApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account, final int page) {
        return apiClient.fetchTransactions(account, page);
    }
}
