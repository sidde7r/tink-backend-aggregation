package se.tink.backend.aggregation.agents.nxgen.uk.revolut.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.RevolutApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class RevolutTransactionFetcher implements TransactionKeyPaginator<TransactionalAccount, Date> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<Date> getTransactionsFor(TransactionalAccount account, Date key) {
        return null;
    }
}
