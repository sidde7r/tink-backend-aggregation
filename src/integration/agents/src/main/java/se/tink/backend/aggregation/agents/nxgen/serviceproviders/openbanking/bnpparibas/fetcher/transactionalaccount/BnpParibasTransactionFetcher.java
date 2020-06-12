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

    private static final long MAX_NUM_MONTHS_FOR_FETCH = 13L;

    private final BnpParibasApiBaseClient apiClient;
    private final Clock clock;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        final LocalDate oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.isAfter(toDateLocal)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final LocalDate limitedFromDate =
                oldestDateForFetch.isAfter(fromDateLocal) ? oldestDateForFetch : fromDateLocal;

        return apiClient.getTransactions(account.getApiIdentifier(), limitedFromDate, toDateLocal);
    }

    private LocalDate getOldestDateForTransactionFetch() {
        return LocalDate.now(clock).minusMonths(MAX_NUM_MONTHS_FOR_FETCH);
    }

    private LocalDate getLocalDateFromDate(Date date) {
        return date.toInstant().atZone(clock.getZone()).toLocalDate();
    }
}
