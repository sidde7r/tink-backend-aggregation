package se.tink.backend.aggregation.nxgen.controllers.refresh.loan;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.TransactionRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.metrics.MetricId;

public final class LoanRefreshController implements AccountRefresher, TransactionRefresher {
    private static final MetricId.MetricLabels METRIC_ACCOUNT_TYPE = new MetricId.MetricLabels()
            .add(AccountRefresher.METRIC_ACCOUNT_TYPE, "loan");

    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final AccountFetcher<LoanAccount> loanFetcher;
    private final TransactionFetcher<LoanAccount> transactionFetcher;

    private Collection<LoanAccount> loans;

    public LoanRefreshController(MetricRefreshController metricRefreshController, UpdateController updateController,
            AccountFetcher<LoanAccount> loanFetcher) {
        this(metricRefreshController, updateController, loanFetcher, null);
    }

    public LoanRefreshController(MetricRefreshController metricRefreshController, UpdateController updateController,
            AccountFetcher<LoanAccount> loanFetcher, TransactionFetcher<LoanAccount> transactionFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.loanFetcher = Preconditions.checkNotNull(loanFetcher);
        this.transactionFetcher = transactionFetcher;
    }

    @Override
    public void refreshAccounts() {
        MetricRefreshAction action = metricRefreshController.buildAction(AccountRefresher.METRIC_ID
                .label(METRIC_ACCOUNT_TYPE), AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Collection<LoanAccount> accounts = getLoans();
            accounts.forEach(updateController::updateAccount);

            action.count(accounts.size());
            action.completed();
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    @Override
    public void refreshTransactions() {
        if (transactionFetcher == null) {
            return;
        }

        MetricRefreshAction action = metricRefreshController.buildAction(TransactionRefresher.METRIC_ID
                .label(METRIC_ACCOUNT_TYPE), TransactionRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            getLoans().forEach(account -> {
                List<AggregationTransaction> transactions = fetchTransactionsFor(account);
                updateController.updateTransactions(account, transactions);

                action.count(transactions.size());
            });

            action.completed();
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    private Collection<LoanAccount> getLoans() {
        if (loans == null) {
            loans = Optional.ofNullable(loanFetcher.fetchAccounts()).orElse(Collections.emptyList());
        }

        return loans;
    }

    private List<AggregationTransaction> fetchTransactionsFor(final LoanAccount account) {
        return Optional.ofNullable(transactionFetcher.fetchTransactionsFor(account)).orElse(Collections.emptyList());
    }
}
