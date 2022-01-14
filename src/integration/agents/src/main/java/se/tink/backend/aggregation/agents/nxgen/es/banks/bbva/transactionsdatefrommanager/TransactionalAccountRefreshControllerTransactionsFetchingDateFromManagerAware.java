package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountRefreshControllerTransactionsFetchingDateFromManagerAware
        extends TransactionalAccountRefreshController {

    private final TransactionsFetchingDateFromManager dateFromManager;

    public TransactionalAccountRefreshControllerTransactionsFetchingDateFromManagerAware(
            MetricRefreshController metricController,
            UpdateController updateController,
            AccountFetcher<TransactionalAccount> accountFetcher,
            TransactionFetcher<TransactionalAccount> transactionFetcher,
            TransactionsFetchingDateFromManager dateFromManager) {
        super(metricController, updateController, accountFetcher, transactionFetcher);
        this.dateFromManager = dateFromManager;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        FetchAccountsResponse response = super.fetchCheckingAccounts();
        if (response.getAccounts().isEmpty()) {
            dateFromManager.cleanCheckingAccountsFetchingLastSuccessDate();
        }
        return response;
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        FetchTransactionsResponse response = super.fetchCheckingTransactions();
        dateFromManager.refreshCheckingAccountsFetchingLastSuccessDate();
        return response;
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        FetchAccountsResponse response = super.fetchSavingsAccounts();
        if (response.getAccounts().isEmpty()) {
            dateFromManager.cleanSavingsAccountsFetchingLastSuccessDate();
        }
        return response;
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        FetchTransactionsResponse response = super.fetchSavingsTransactions();
        dateFromManager.refreshSavingsAccountsFetchingLastSuccessDate();
        return response;
    }
}
