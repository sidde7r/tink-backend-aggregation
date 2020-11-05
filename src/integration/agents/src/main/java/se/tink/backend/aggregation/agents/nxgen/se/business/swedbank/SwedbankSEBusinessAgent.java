package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.fetchers.transactional.SwedbankSEBusinessTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class SwedbankSEBusinessAgent extends SwedbankSEAgent {

    @Inject
    public SwedbankSEBusinessAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        SwedbankDefaultTransactionalAccountFetcher transactionalFetcher =
                new SwedbankSEBusinessTransactionalAccountFetcher(apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher),
                        transactionalFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalFetcher,
                transactionFetcherController);
    }
}
