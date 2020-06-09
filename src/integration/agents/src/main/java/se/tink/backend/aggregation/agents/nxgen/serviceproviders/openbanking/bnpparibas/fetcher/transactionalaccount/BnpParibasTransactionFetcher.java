package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BnpParibasTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private static final long NUM_MONTHS_FOR_FETCH = 13L;

    private final BnpParibasApiBaseClient apiClient;
    private final Clock clock;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final Date oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.after(toDate)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final Date limitedFromDate =
                oldestDateForFetch.after(fromDate) ? oldestDateForFetch : fromDate;

        return apiClient.getTransactions(account.getAccountNumber(), limitedFromDate, toDate);
    }

    private Date getOldestDateForTransactionFetch() {
        final LocalDate localDate = LocalDate.now(clock).minusMonths(NUM_MONTHS_FOR_FETCH);

        return Date.from(localDate.atStartOfDay(clock.getZone()).toInstant());
    }
}
