package se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount;

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
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.metrics.MetricId;

public final class TransactionalAccountRefreshController implements AccountRefresher, TransactionRefresher {
    private static final MetricId.MetricLabels METRIC_ACCOUNT_TYPE = new MetricId.MetricLabels()
            .add(AccountRefresher.METRIC_ACCOUNT_TYPE, "transactional");

    private final MetricRefreshController metricController;
    private final UpdateController updateController;
    private final AccountFetcher<TransactionalAccount> accountFetcher;
    private final TransactionFetcher<TransactionalAccount> transactionFetcher;

    private Collection<TransactionalAccount> accounts;

    public TransactionalAccountRefreshController(MetricRefreshController metricController, UpdateController updateController,
            AccountFetcher<TransactionalAccount> accountFetcher, TransactionFetcher<TransactionalAccount> transactionFetcher) {
        this.metricController = Preconditions.checkNotNull(metricController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.accountFetcher = Preconditions.checkNotNull(accountFetcher);
        this.transactionFetcher = Preconditions.checkNotNull(transactionFetcher);
    }

    @Override
    public void refreshAccounts() {
        MetricRefreshAction action = metricController.buildAction(AccountRefresher.METRIC_ID.label(METRIC_ACCOUNT_TYPE),
                AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Collection<TransactionalAccount> accounts = fetchAccounts();
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
        MetricRefreshAction action = metricController.buildAction(TransactionRefresher.METRIC_ID
                .label(METRIC_ACCOUNT_TYPE), TransactionRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            fetchAccounts().forEach(a -> {
                List<AggregationTransaction> transactions = fetchTransactionsFor(a);
                updateController.updateTransactions(a, transactions);

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

    private Collection<TransactionalAccount> fetchAccounts() {
        if (accounts == null) {
            accounts = Optional.ofNullable(accountFetcher.fetchAccounts()).orElse(Collections.emptyList());
        }

        return accounts;
    }

    private List<AggregationTransaction> fetchTransactionsFor(final TransactionalAccount account) {
        return Optional.ofNullable(transactionFetcher.fetchTransactionsFor(account)).orElseGet(Collections::emptyList);
    }
}
