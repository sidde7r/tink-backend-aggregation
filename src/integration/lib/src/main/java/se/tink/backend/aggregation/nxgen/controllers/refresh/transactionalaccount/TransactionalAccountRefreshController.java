package se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount;

import com.google.common.base.Preconditions;
import java.util.*;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.TransactionRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.pair.Pair;

public final class TransactionalAccountRefreshController
        implements AccountRefresher, TransactionRefresher {
    private static final MetricId.MetricLabels METRIC_ACCOUNT_TYPE =
            new MetricId.MetricLabels().add(AccountRefresher.METRIC_ACCOUNT_TYPE, "transactional");

    private final MetricRefreshController metricController;
    private final UpdateController updateController;
    private final AccountFetcher<TransactionalAccount> accountFetcher;
    private final TransactionFetcher<TransactionalAccount> transactionFetcher;

    private Collection<TransactionalAccount> accounts;

    public TransactionalAccountRefreshController(
            MetricRefreshController metricController,
            UpdateController updateController,
            AccountFetcher<TransactionalAccount> accountFetcher,
            TransactionFetcher<TransactionalAccount> transactionFetcher) {
        this.metricController = Preconditions.checkNotNull(metricController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.accountFetcher = Preconditions.checkNotNull(accountFetcher);
        this.transactionFetcher = Preconditions.checkNotNull(transactionFetcher);
    }

    @Override
    public Map<Account, AccountFeatures> fetchAccounts() {
        MetricRefreshAction action =
                metricController.buildAction(
                        AccountRefresher.METRIC_ID.label(METRIC_ACCOUNT_TYPE),
                        AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Map<Account, AccountFeatures> systemAccounts = new HashMap<>();

            for (TransactionalAccount account : getAccounts()) {
                Pair<Account, AccountFeatures> accounts = updateController.updateAccount(account);
                if (accounts != null) {
                    systemAccounts.put(
                            updateController.updateAccount(account).first,
                            AccountFeatures.createEmpty());
                }
            }

            action.count(systemAccounts.size());
            action.completed();
            return systemAccounts;
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    @Override
    public Map<Account, List<Transaction>> fetchTransactions() {
        MetricRefreshAction action =
                metricController.buildAction(
                        TransactionRefresher.METRIC_ID.label(METRIC_ACCOUNT_TYPE),
                        TransactionRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Map<Account, List<Transaction>> transactionsMap = new HashMap<>();

            for (TransactionalAccount account : getAccounts()) {
                List<AggregationTransaction> transactions = fetchTransactionsFor(account);
                Pair<Account, List<Transaction>> accountTransactions =
                        updateController.updateTransactions(account, transactions);
                if (accountTransactions != null) {
                    transactionsMap.put(accountTransactions.first, accountTransactions.second);
                }
                action.count(transactions.size());
            }
            action.completed();
            return transactionsMap;
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    private Collection<TransactionalAccount> getAccounts() {
        if (accounts == null) {
            accounts =
                    Optional.ofNullable(accountFetcher.fetchAccounts())
                            .orElse(Collections.emptyList());
        }

        return accounts;
    }

    private List<AggregationTransaction> fetchTransactionsFor(final TransactionalAccount account) {
        return Optional.ofNullable(transactionFetcher.fetchTransactionsFor(account))
                .orElseGet(Collections::emptyList);
    }
}
