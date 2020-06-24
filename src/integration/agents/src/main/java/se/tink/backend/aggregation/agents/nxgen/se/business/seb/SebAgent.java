package se.tink.backend.aggregation.agents.nxgen.se.business.seb;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.TransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;

public class SebAgent extends SebBaseAgent {
    @Inject
    public SebAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new SebConfiguration(
                        Optional.ofNullable(
                                        componentProvider
                                                .getCredentialsRequest()
                                                .getCredentials()
                                                .getField(Key.CORPORATE_ID))
                                .map(s -> s.replace("-", ""))
                                .map(String::trim)
                                .orElse("")));
    }

    @Override
    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountFetcher(apiClient, sebSessionStorage, sebConfiguration),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new TransactionFetcher(apiClient))));
    }
}
