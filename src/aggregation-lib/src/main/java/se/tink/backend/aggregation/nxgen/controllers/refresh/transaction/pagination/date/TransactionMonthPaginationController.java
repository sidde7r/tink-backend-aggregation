package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.TimeZone;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionMonthPaginationController<A extends Account> implements TransactionPaginator<A> {
    static final int MAX_CONSECUTIVE_EMPTY_PAGES = 3;
    private final LocalDate nowInLocalDate = LocalDate.now(TimeZone.getTimeZone("Europe/Stockholm").toZoneId());
    private final TransactionMonthPaginator paginator;
    private A currentAccount;
    private LocalDate dateToFetch;
    private int consecutiveEmptyFetches = 0;

    public TransactionMonthPaginationController(TransactionMonthPaginator paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
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
            return Collections.emptyList();
        }

        consecutiveEmptyFetches = 0;
        return transactions;
    }

    @Override
    public boolean canFetchMoreFor(A account) {
        resetStateIfAccountChanged(account);

        return consecutiveEmptyFetches < MAX_CONSECUTIVE_EMPTY_PAGES;
    }

    private void resetStateIfAccountChanged(A account) {
        Preconditions.checkNotNull(account);

        if (Objects.equals(currentAccount, account)) {
            return;
        }

        currentAccount = account;
        consecutiveEmptyFetches = 0;
        dateToFetch = nowInLocalDate;
    }
}
