package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AbnAmroTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final AbnAmroApiClient apiClient;

    public AbnAmroTransactionFetcher(final AbnAmroApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        String accountId = account.getAccountNumber();
        if (Objects.isNull(key)) {
            Calendar minDate = Calendar.getInstance();
            minDate.add(Calendar.MONTH, -18);
            // fromDate cannot be older than 18 months
            final Date toDate = new Date();
            return apiClient.fetchTransactionsByDate(accountId, minDate.getTime(), toDate);
        }
        return apiClient.fetchTransactionsByKey(key, accountId);
    }
}
