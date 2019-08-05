package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class KbcTransactionFetcher extends BerlinGroupTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {
    protected final KbcApiClient apiClient;

    public KbcTransactionFetcher(final KbcApiClient apiClient) {
        super(apiClient);
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactions(account, fromDate, toDate);
    }
}
