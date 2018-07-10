package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.log.AggregationLogger;
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
    public Collection<? extends Transaction> fetchTransactionsFor(A account) {
        Preconditions.checkState(canFetchMoreFor(account),
                "Fetching more transactions when canFetchMore() returns false is not allowed");

        toDate = calculateToDate();
        fromDate = DateUtils.addMonths(toDate, -MONTHS_TO_FETCH);

        Collection<? extends Transaction> transactions = paginator.getTransactionsFor(account, fromDate, toDate);

        if (transactions == null || transactions.isEmpty()) {
            log.info(String.format("Couldn't find any transactions for account with bankIdentifier: %s",
                    account.getBankIdentifier()));
            consecutiveEmptyPages++;

            // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
            String bankCode = account.getTemporaryStorage().get("BANK_CODE");
            // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
            if ("9680".equals(bankCode)) {
                log.info(String.format("Couldn't find any transactions for account with bankIdentifier: %s [%d]",
                        account.getBankIdentifier(),
                        consecutiveEmptyPages));
            }

            return Collections.emptyList();
        }

        log.info(String.format("Fetched %s transactions for account with bankIdentifier: %s", transactions.size(),
                account.getBankIdentifier()));

        consecutiveEmptyPages = 0;
        return transactions;
    }

    @Override
    public boolean canFetchMoreFor(A account) {

        // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
        String bankCode = account.getTemporaryStorage().get("BANK_CODE");
        // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
        if ("9680".equals(bankCode)) {
            log.info(String.format("canFetchMoreFor: %s [%d] = %b",
                    account.getBankIdentifier(),
                    consecutiveEmptyPages,
                    (consecutiveEmptyPages < MAX_CONSECUTIVE_EMPTY_PAGES)));
        }

        resetStateIfAccountChanged(account);

        return consecutiveEmptyPages < MAX_CONSECUTIVE_EMPTY_PAGES;
    }

    private void resetStateIfAccountChanged(Account account) {
        Preconditions.checkNotNull(account);

        // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
        String bankCode = account.getTemporaryStorage().get("BANK_CODE");
        // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
        if ("9680".equals(bankCode)) {
            log.info(String.format("resetStateIfAccountChanged: %s, same account? %b %b",
                    account.getBankIdentifier(),
                    (currentAccount != null && account.equals(currentAccount)),
                    Objects.equals(currentAccount, account)));
        }

        if (Objects.equals(currentAccount, account)) {
            return;
        }

        // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
        if ("9680".equals(bankCode)) {
            log.info(String.format("resetting state as account is changed: new[%s], same account? current[%s]",
                    account.getUniqueIdentifier(),
                    (currentAccount != null ? currentAccount.getUniqueIdentifier() : "N/A")));
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
