package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class TransactionFetcherController<A extends Account> implements TransactionFetcher<A> {
    private final TransactionPaginator<A> paginator;
    private final UpcomingTransactionFetcher upcomingTransactionFetcher;
    private final TransactionPaginationHelper paginationHelper;

    // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
    private static final AggregationLogger log = new AggregationLogger(TransactionDatePaginationController.class);

    public TransactionFetcherController(
            TransactionPaginationHelper paginationHelper, TransactionPaginator<A> paginator) {
        this(paginationHelper, paginator, null);
    }

    public TransactionFetcherController(
            TransactionPaginationHelper paginationHelper,
            TransactionPaginator<A> paginator,
            UpcomingTransactionFetcher upcomingTransactionFetcher) {
        this.paginationHelper = Preconditions.checkNotNull(paginationHelper);
        this.paginator = Preconditions.checkNotNull(paginator);
        this.upcomingTransactionFetcher = upcomingTransactionFetcher; // Optional
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(A account) {
        Preconditions.checkNotNull(account);
        List<AggregationTransaction> transactions = Lists.newArrayList();

        transactions.addAll(fetchUpcomingTransactionsFor(account));

        // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
        String bankCode = account.getTemporaryStorage().get("BANK_CODE");
        do {
            // TODO: this is temporary just to be able to log credit card transaction fetching for "no-storebrand" = "9680"
            if ("9680".equals(bankCode)) {
                log.info(String.format("Can fetch more transactions for account with bankIdentifier: %s [%b]",
                        account.getBankIdentifier(),
                        paginator.canFetchMoreFor(account)));
            }

            Collection<? extends Transaction> batchTransactions = paginator.fetchTransactionsFor(account);
            if (batchTransactions != null) {
                transactions.addAll(batchTransactions);
            }
        } while (paginator.canFetchMoreFor(account)
                && !paginationHelper.isContentWithRefresh(account, transactions));

        return transactions;
    }

    private Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(Account account) {
        return Optional.ofNullable(upcomingTransactionFetcher)
                .flatMap(
                        upcomingTransactionFetcher ->
                                Optional.of(account)
                                        .map(t -> upcomingTransactionFetcher.fetchUpcomingTransactionsFor(t)))
                .orElseGet(Collections::emptyList);
    }
}
