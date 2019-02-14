package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.accounts.checking;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.statement.MT940Statement;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class FinTsTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private FinTsApiClient apiClient;

    public FinTsTransactionFetcher(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        if (shouldStopFetching(toDate)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        Collection<? extends Transaction> transactions =
                apiClient
                        .getTransactions(account.getAccountNumber(), fromDate, toDate)
                        .stream()
                        .map(MT940Statement::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions);
    }

    private boolean shouldStopFetching(Date toDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 2);
        return calendar.getTime().before(toDate);
    }
}
