package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount;

import java.util.Date;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class SkandiaTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private SkandiaApiClient apiClient;
    private LocalDateTimeSource localDateTimeSource;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        Date currentDate = Date.from(localDateTimeSource.getInstant());

        GetTransactionsResponse transactions =
                apiClient.getTransactions(
                        account.getApiIdentifier(),
                        currentDate,
                        getPendingToDate(currentDate),
                        QueryValues.PENDING);
        GetTransactionsResponse bookedTransactions =
                apiClient.getTransactions(
                        account.getApiIdentifier(), fromDate, toDate, QueryValues.BOOKED);
        transactions.setBooked(bookedTransactions.getBooked());
        return transactions;
    }

    // Get 3 months date ahead from current for pending transaction
    private Date getPendingToDate(Date date) {
        return DateUtils.addMonths(date, 3);
    }
}
