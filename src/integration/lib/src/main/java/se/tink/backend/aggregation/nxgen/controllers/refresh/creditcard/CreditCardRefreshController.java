package se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.TransactionRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.pair.Pair;

public final class CreditCardRefreshController
        implements AccountRefresher, TransactionRefresher, RefreshCreditCardAccountsExecutor {
    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final AccountFetcher<CreditCardAccount> accountFetcher;
    private final TransactionFetcher<CreditCardAccount> transactionFetcher;

    private Collection<CreditCardAccount> accounts;

    public CreditCardRefreshController(
            MetricRefreshController metricRefreshController,
            UpdateController updateController,
            AccountFetcher<CreditCardAccount> accountFetcher,
            TransactionFetcher<CreditCardAccount> transactionFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.accountFetcher = Preconditions.checkNotNull(accountFetcher);
        this.transactionFetcher = Preconditions.checkNotNull(transactionFetcher);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        List<Account> accounts = new ArrayList<>(this.fetchAccounts().keySet());
        return new FetchAccountsResponse(accounts);
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return new FetchTransactionsResponse(this.fetchTransactions());
    }

    @Override
    public Map<Account, AccountFeatures> fetchAccounts() {
        MetricRefreshAction action =
                metricRefreshController.buildAction(
                        AccountRefresher.METRIC_ID, AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Map<Account, AccountFeatures> systemAccounts = new HashMap<>();

            for (CreditCardAccount account : fetchCreditCards()) {
                Pair<Account, AccountFeatures> accounts = updateController.updateAccount(account);
                if (accounts != null) {
                    systemAccounts.put(accounts.first, AccountFeatures.createEmpty());
                }
            }

            action.count(accounts.size());
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
                metricRefreshController.buildAction(
                        TransactionRefresher.METRIC_ID,
                        TransactionRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Map<Account, List<Transaction>> transactionsMap = new HashMap<>();

            fetchCreditCards()
                    .forEach(
                            account -> {
                                List<AggregationTransaction> transactions =
                                        fetchTransactionsFor(account);
                                Pair<Account, List<Transaction>> accountTransactions =
                                        updateController.updateTransactions(account, transactions);
                                if (accountTransactions != null) {
                                    transactionsMap.put(
                                            accountTransactions.first, accountTransactions.second);
                                }

                                action.count(transactions.size());
                            });

            action.completed();
            return transactionsMap;
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    private Collection<CreditCardAccount> fetchCreditCards() {
        if (accounts == null) {
            accounts =
                    Optional.ofNullable(accountFetcher.fetchAccounts())
                            .orElseGet(Collections::emptyList);
        }

        return accounts;
    }

    private List<AggregationTransaction> fetchTransactionsFor(final CreditCardAccount account) {
        return Optional.ofNullable(transactionFetcher.fetchTransactionsFor(account))
                .orElseGet(Collections::emptyList);
    }
}
