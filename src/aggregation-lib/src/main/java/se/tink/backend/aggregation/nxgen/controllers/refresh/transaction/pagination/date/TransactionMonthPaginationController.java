package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionMonthPaginationController<A extends Account> implements TransactionPaginator<A> {
    protected static final int MAX_CONSECUTIVE_EMPTY_PAGES = 4;
    protected static final int MAX_TOTAL_EMPTY_PAGES = 24;
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Stockholm");
    private final LocalDate nowInLocalDate;
    private final TransactionMonthPaginator paginator;
    private A currentAccount;
    private LocalDate dateToFetch;
    private int consecutiveEmptyFetches = 0;
    private int totalEmptyFetches = 0;
    private boolean haveFoundSomething;

    public TransactionMonthPaginationController(TransactionMonthPaginator paginator) {
        this(paginator, DEFAULT_ZONE_ID);
    }

    public TransactionMonthPaginationController(TransactionMonthPaginator paginator, ZoneId zoneId) {
        this.paginator = Preconditions.checkNotNull(paginator);
        nowInLocalDate = LocalDate.now(zoneId);
    }

    @Override
    public Collection<? extends Transaction> fetchTransactionsFor(A account) {
        Preconditions.checkState(canFetchMoreFor(account),
                "Fetching more transactions when canFetchMore() returns false is not allowed");
        resetStateIfAccountChanged(account);

        Collection<? extends Transaction> transactions = paginator.getTransactionsFor(
                account, Year.from(dateToFetch), Month.from(dateToFetch));

        dateToFetch = dateToFetch.minusMonths(1);

        if (transactions == null || transactions.isEmpty()) {
            consecutiveEmptyFetches++;
            totalEmptyFetches++;
            return Collections.emptyList();
        }

        consecutiveEmptyFetches = 0;
        haveFoundSomething = true;
        return transactions;
    }

    @Override
    public boolean canFetchMoreFor(A account) {
        resetStateIfAccountChanged(account);

        return haveFoundSomething ? consecutiveEmptyFetches < MAX_CONSECUTIVE_EMPTY_PAGES : totalEmptyFetches < MAX_TOTAL_EMPTY_PAGES;
    }

    private void resetStateIfAccountChanged(A account) {
        Preconditions.checkNotNull(account);

        if (Objects.equals(currentAccount, account)) {
            return;
        }

        currentAccount = account;
        consecutiveEmptyFetches = 0;
        totalEmptyFetches = 0;
        haveFoundSomething = false;
        dateToFetch = nowInLocalDate;
    }
}
