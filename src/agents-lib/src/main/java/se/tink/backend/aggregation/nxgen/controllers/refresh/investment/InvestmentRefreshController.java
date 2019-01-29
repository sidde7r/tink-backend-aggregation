package se.tink.backend.aggregation.nxgen.controllers.refresh.investment;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountRefresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.pair.Pair;

import java.util.*;

public final class InvestmentRefreshController implements AccountRefresher {
    private static final MetricId.MetricLabels METRIC_ACCOUNT_TYPE = new MetricId.MetricLabels()
            .add(AccountRefresher.METRIC_ACCOUNT_TYPE, "investment");

    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final AccountFetcher<InvestmentAccount> investmentFetcher;

    private Collection<InvestmentAccount> investments;

    public InvestmentRefreshController(MetricRefreshController metricRefreshController, UpdateController updateController,
            AccountFetcher<InvestmentAccount> investmentFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.investmentFetcher = Preconditions.checkNotNull(investmentFetcher);
    }

    @Override
    public Map<Account, AccountFeatures> refreshAccounts() {
        MetricRefreshAction action = metricRefreshController.buildAction(AccountRefresher.METRIC_ID
                .label(METRIC_ACCOUNT_TYPE), AccountRefresher.METRIC_COUNTER_BUCKETS);

        try {
            action.start();

            Map<Account, AccountFeatures> systemAccounts = new HashMap<>();

            for (InvestmentAccount account : getInvestments()) {
                Pair<Account, AccountFeatures> systemAccount = updateController.updateAccount(account);
                systemAccounts.put(systemAccount.first, systemAccount.second);
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

    private Collection<InvestmentAccount> getInvestments() {
        if (investments == null) {
            investments = Optional.ofNullable(investmentFetcher.fetchAccounts()).orElse(Collections.emptyList());
        }

        return investments;
    }
}
