package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher;

import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AbnAmroTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final AbnAmroApiClient apiClient;

    public AbnAmroTransactionFetcher(final AbnAmroApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            final TransactionalAccount account, Date fromDate, Date toDate) {
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.MONTH, -18);

        // fromDate cannot be older than 18 months
        if (fromDate.before(minDate.getTime())) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        return apiClient.fetchTransactions(fromDate, toDate).getTransactionList();
    }
}
