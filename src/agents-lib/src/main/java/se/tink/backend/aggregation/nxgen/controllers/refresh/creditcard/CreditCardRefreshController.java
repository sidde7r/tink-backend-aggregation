package se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard;

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
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.metrics.MetricId;

public final class CreditCardRefreshController implements AccountRefresher, TransactionRefresher {
    private static final MetricId.MetricLabels METRIC_ACCOUNT_TYPE = new MetricId.MetricLabels()
            .add(AccountRefresher.METRIC_ACCOUNT_TYPE, "credit_card");

    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final AccountFetcher<CreditCardAccount> accountFetcher;
    private final TransactionFetcher<CreditCardAccount> transactionFetcher;

    private Collection<CreditCardAccount> accounts;

    public CreditCardRefreshController(MetricRefreshController metricRefreshController, UpdateController updateController,
            AccountFetcher<CreditCardAccount> accountFetcher, TransactionFetcher<CreditCardAccount> transactionFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.accountFetcher = Preconditions.checkNotNull(accountFetcher);
        this.transactionFetcher = Preconditions.checkNotNull(transactionFetcher);
    }

    @Override
    public void refreshAccounts() {
        MetricRefreshAction action = metricRefreshController.buildAction(AccountRefresher.METRIC_ID
                .label(METRIC_ACCOUNT_TYPE), AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Collection<CreditCardAccount> accounts = fetchCreditCards();
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
        MetricRefreshAction action = metricRefreshController.buildAction(TransactionRefresher.METRIC_ID
                .label(METRIC_ACCOUNT_TYPE), TransactionRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            fetchCreditCards().forEach(account -> {
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

    private Collection<CreditCardAccount> fetchCreditCards() {
        if (accounts == null) {
            accounts = Optional.ofNullable(accountFetcher.fetchAccounts()).orElse(Collections.emptyList());
        }

        return accounts;
    }

    private List<AggregationTransaction> fetchTransactionsFor(final CreditCardAccount account) {
        return Optional.ofNullable(transactionFetcher.fetchTransactionsFor(account)).orElse(Collections.emptyList());
    }
}
