package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SkandiaTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private SkandiaApiClient apiClient;

    public SkandiaTransactionFetcher(SkandiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        // there is no "both" booking status
        GetTransactionsResponse transactions =
                apiClient.getTransactions(
                        account.getApiIdentifier(), fromDate, toDate, QueryValues.PENDING);
        GetTransactionsResponse bookedTransactions =
                apiClient.getTransactions(
                        account.getApiIdentifier(), fromDate, toDate, QueryValues.BOOKED);
        transactions.setBooked(bookedTransactions.getBooked());
        return transactions;
    }
}
