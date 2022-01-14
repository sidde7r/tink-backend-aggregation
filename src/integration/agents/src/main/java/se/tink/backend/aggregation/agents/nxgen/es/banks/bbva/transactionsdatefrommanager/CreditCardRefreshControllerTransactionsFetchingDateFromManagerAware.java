package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardRefreshControllerTransactionsFetchingDateFromManagerAware
        extends CreditCardRefreshController {

    private final TransactionsFetchingDateFromManager dateFromManager;

    public CreditCardRefreshControllerTransactionsFetchingDateFromManagerAware(
            MetricRefreshController metricRefreshController,
            UpdateController updateController,
            AccountFetcher<CreditCardAccount> accountFetcher,
            TransactionFetcher<CreditCardAccount> transactionFetcher,
            TransactionsFetchingDateFromManager dateFromManager) {
        super(metricRefreshController, updateController, accountFetcher, transactionFetcher);
        this.dateFromManager = dateFromManager;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        FetchAccountsResponse response = super.fetchCreditCardAccounts();
        if (response.getAccounts().isEmpty()) {
            dateFromManager.cleanCreditCardsFetchingLastSuccessDate();
        }
        return response;
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        FetchTransactionsResponse response = super.fetchCreditCardTransactions();
        dateFromManager.refreshCreditCardsFetchingLastSuccessDate();
        return response;
    }
}
