package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.initialtransactions;

import java.time.LocalDate;
import java.time.ZoneId;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class InitialTransactionsFromCertainDateFetcherController<
                A extends Account, R extends PaginatorResponse>
        implements InitialTransactionsFetcher<A, R> {
    private static final LocalDate JANUARY_FIRST_NINETEEN_SEVENTY = LocalDate.of(1970, 1, 1);

    private final InitialTransactionsFromCertainDateFetcher<A, R> fetcher;
    private final TransactionPaginationHelper paginationHelper;

    public InitialTransactionsFromCertainDateFetcherController(
            InitialTransactionsFromCertainDateFetcher<A, R> fetcher,
            TransactionPaginationHelper paginationHelper) {
        this.fetcher = fetcher;
        this.paginationHelper = paginationHelper;
    }

    @Override
    public R fetchInitialTransactionsFor(A account) {
        LocalDate fromDate =
                paginationHelper
                        .getContentWithRefreshDate(account)
                        .map(
                                date ->
                                        date.toInstant()
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                                .minusDays(10))
                        .orElse(JANUARY_FIRST_NINETEEN_SEVENTY);

        return fetcher.fetchInitialTransactionsFor(account, fromDate);
    }
}
