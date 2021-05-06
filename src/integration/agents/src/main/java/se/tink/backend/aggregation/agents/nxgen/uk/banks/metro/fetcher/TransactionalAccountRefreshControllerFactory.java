package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher;

import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountRefreshControllerFactory {

    private final AccountFetcher<TransactionalAccount> accountFetcher;

    private final TransactionFetcher<TransactionalAccount> transactionFetcher;

    public TransactionalAccountRefreshControllerFactory(
            AccountFetcher<TransactionalAccount> accountFetcher,
            TransactionFetcher<TransactionalAccount> transactionFetcher) {
        this.accountFetcher = accountFetcher;
        this.transactionFetcher = transactionFetcher;
    }

    public TransactionalAccountRefreshController create(
            MetricRefreshController metricController, UpdateController updateController) {
        return new TransactionalAccountRefreshController(
                metricController, updateController, accountFetcher, transactionFetcher);
    }
}
