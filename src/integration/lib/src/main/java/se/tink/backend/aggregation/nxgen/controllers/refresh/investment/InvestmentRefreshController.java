package se.tink.backend.aggregation.nxgen.controllers.refresh.investment;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.TransactionRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.pair.Pair;

public final class InvestmentRefreshController
        implements AccountRefresher, TransactionRefresher, RefreshInvestmentAccountsExecutor {

    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final AccountFetcher<InvestmentAccount> investmentFetcher;

    private Collection<InvestmentAccount> investments;
    private TransactionFetcher<InvestmentAccount> transactionFetcher;

    public InvestmentRefreshController(
            MetricRefreshController metricRefreshController,
            UpdateController updateController,
            AccountFetcher<InvestmentAccount> investmentFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.investmentFetcher = Preconditions.checkNotNull(investmentFetcher);
    }

    public InvestmentRefreshController(
            MetricRefreshController metricRefreshController,
            UpdateController updateController,
            AccountFetcher<InvestmentAccount> investmentFetcher,
            TransactionFetcher<InvestmentAccount> transactionFetcher) {
        this(metricRefreshController, updateController, investmentFetcher);
        this.transactionFetcher = Preconditions.checkNotNull(transactionFetcher);
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return new FetchInvestmentAccountsResponse(this.fetchAccounts());
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(this.fetchTransactions());
    }

    @Deprecated
    @Override
    public Map<Account, AccountFeatures> fetchAccounts() {
        MetricRefreshAction action =
                metricRefreshController.buildAction(
                        AccountRefresher.METRIC_ID, AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Map<Account, AccountFeatures> systemAccounts = new HashMap<>();

            for (InvestmentAccount account : getInvestmentAccounts()) {
                Pair<Account, AccountFeatures> systemAccount =
                        updateController.updateAccount(account);
                if (systemAccount != null) {
                    systemAccounts.put(systemAccount.first, systemAccount.second);
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

    @Deprecated
    @Override
    public Map<Account, List<Transaction>> fetchTransactions() {
        MetricRefreshAction action =
                metricRefreshController.buildAction(
                        TransactionRefresher.METRIC_ID,
                        TransactionRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            // Added Merger to the Collectors.toMap to remove duplicates.
            Map<Account, List<Transaction>> transactionsMap =
                    getInvestmentAccounts().stream()
                            .map(this::fetchTransactionsFor)
                            .filter(Objects::nonNull)
                            .peek((pair -> action.count(pair.second.size())))
                            .collect(
                                    Collectors.toMap(
                                            pair -> pair.first,
                                            pair -> pair.second,
                                            (transactionList1, transactionList2) ->
                                                    transactionList1));

            action.completed();
            return transactionsMap;
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    private Pair<Account, List<Transaction>> fetchTransactionsFor(InvestmentAccount account) {
        List<AggregationTransaction> transactions =
                Optional.ofNullable(transactionFetcher)
                        .map(fetcher -> fetcher.fetchTransactionsFor(account))
                        .orElseGet(Collections::emptyList);
        return updateController.updateTransactions(account, transactions);
    }

    private Collection<InvestmentAccount> getInvestmentAccounts() {
        if (investments == null) {
            investments =
                    Optional.ofNullable(investmentFetcher.fetchAccounts())
                            .orElseGet(Collections::emptyList);
        }

        return investments;
    }
}
