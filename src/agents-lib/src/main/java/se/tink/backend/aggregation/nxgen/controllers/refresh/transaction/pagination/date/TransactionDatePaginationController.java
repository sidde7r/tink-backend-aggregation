package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

public class TransactionDatePaginationController<A extends Account> implements TransactionPaginator<A> {
    private static final AggregationLogger log = new AggregationLogger(TransactionDatePaginationController.class);
    static final int MAX_CONSECUTIVE_EMPTY_PAGES = 2;
    private static final int MONTHS_TO_FETCH = 3;

    private final TransactionDatePaginator<A> paginator;

    private Account currentAccount;
    private Date fromDate;
    private Date toDate;
    private int consecutiveEmptyPages = 0;

    public TransactionDatePaginationController(TransactionDatePaginator<A> paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        resetStateIfAccountChanged(account);

        toDate = calculateToDate();
        fromDate = DateUtils.addMonths(toDate, -MONTHS_TO_FETCH);

        PaginatorResponse response = paginator.getTransactionsFor(account, fromDate, toDate);

        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        if (transactions.isEmpty() && !response.canFetchMore().isPresent()) {
            // Override canFetchMore with consecutive check.

            log.info(String.format("Couldn't find any transactions for account with accountNumber: %s",
                    account.getAccountNumber()));

            consecutiveEmptyPages++;
            return PaginatorResponseImpl.createEmpty(consecutiveEmptyPages < MAX_CONSECUTIVE_EMPTY_PAGES);
        }

        log.info(String.format("Fetched %s transactions for account with accountNumber: %s", transactions.size(),
                account.getAccountNumber()));

        consecutiveEmptyPages = 0;

        if (!response.canFetchMore().isPresent()) {
            // If canFetchMore is not defined we assume we always can fetch more (until we reach an empty page).
            return PaginatorResponseImpl.create(transactions, true);
        }

        return response;
    }

    private void resetStateIfAccountChanged(Account account) {
        Preconditions.checkNotNull(account);

        if (Objects.equals(currentAccount, account)) {
            return;
        }

        currentAccount = account;
        fromDate = null;
        toDate = null;
        consecutiveEmptyPages = 0;
    }

    private Date calculateToDate() {
        if (toDate == null) {
            return new Date(); // Today
        }

        return DateUtils.addDays(fromDate, -1); // Day before the previous fromDate
    }
}
