package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final NordeaBaseApiClient apiClient;

    public NordeaTransactionFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchAccountTransactions(account, fromDate, toDate);
    }
}
