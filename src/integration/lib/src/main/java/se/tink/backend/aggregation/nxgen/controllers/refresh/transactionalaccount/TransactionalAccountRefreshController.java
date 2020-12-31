package se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
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
import se.tink.libraries.pair.Pair;

public final class TransactionalAccountRefreshController
        implements AccountRefresher,
                TransactionRefresher,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final MetricRefreshController metricController;
    private final UpdateController updateController;
    private final AccountFetcher<TransactionalAccount> accountFetcher;
    private final TransactionFetcher<TransactionalAccount> transactionFetcher;

    private Collection<TransactionalAccount> accounts;
    private Collection<Account> cachedAccounts;
    private Map<Account, List<Transaction>> cachedTransactions = Collections.emptyMap();

    private boolean hasRefreshedTransactions = false;
    private boolean hasRefreshedAccounts = false;

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

    private List<Account> getCachedAccounts() {
        if (!hasRefreshedAccounts) {
            this.cachedAccounts = fetchAccounts().keySet();
            hasRefreshedAccounts = true;
        }
        return new ArrayList<>(this.cachedAccounts);
    }

    private Map<Account, List<Transaction>> getCachedTransactions() {
        if (!hasRefreshedTransactions) {
            this.cachedTransactions = this.fetchTransactions();
        }
        return this.cachedTransactions;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        // Short term fix for filtering out Savings accounts
        List<Account> accounts =
                new ArrayList<>(
                        this.getCachedAccounts().stream()
                                .filter(
                                        account ->
                                                account.getType().equals(AccountTypes.CHECKING)
                                                        || account.getType()
                                                                .equals(AccountTypes.OTHER))
                                .collect(Collectors.toList()));

        return new FetchAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(
                this.getCachedTransactions().entrySet().stream()
                        .filter(
                                account ->
                                        account.getKey().getType().equals(AccountTypes.CHECKING)
                                                || account.getKey()
                                                        .getType()
                                                        .equals(AccountTypes.OTHER))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        // Short term fix for filtering out Checking & Other
        List<Account> accounts =
                new ArrayList<>(
                        this.getCachedAccounts().stream()
                                .filter(account -> account.getType().equals(AccountTypes.SAVINGS))
                                .collect(Collectors.toList()));

        return new FetchAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return new FetchTransactionsResponse(
                this.getCachedTransactions().entrySet().stream()
                        .filter(account -> account.getKey().getType().equals(AccountTypes.SAVINGS))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public Map<Account, AccountFeatures> fetchAccounts() {
        MetricRefreshAction action =
                metricController.buildAction(
                        REFRESHER_METRIC_ID, AccountRefresher.METRIC_COUNTER_BUCKETS);

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
                        TransactionRefresher.METRIC_ID,
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
            // Temporary fix to avoid fetching transactions twice
            hasRefreshedTransactions = true;
        }
    }

    private Collection<TransactionalAccount> getAccounts() {
        if (accounts == null) {
            accounts =
                    Optional.ofNullable(accountFetcher.fetchAccounts())
                            .orElseGet(Collections::emptyList);
        }

        return accounts;
    }

    private List<AggregationTransaction> fetchTransactionsFor(final TransactionalAccount account) {
        return Optional.ofNullable(transactionFetcher.fetchTransactionsFor(account))
                .orElseGet(Collections::emptyList);
    }
}
