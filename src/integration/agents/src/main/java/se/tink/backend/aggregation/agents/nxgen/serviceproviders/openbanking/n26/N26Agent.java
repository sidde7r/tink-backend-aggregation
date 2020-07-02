package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26AuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.N26TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.storage.N26Storage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class N26Agent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private final N26Storage storage;
    private final N26ApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public N26Agent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.storage = new N26Storage(persistentStorage);
        this.apiClient = new N26ApiClient(client, getAgentConfig(), storage);
        this.transactionalAccountRefreshController = initTransactionalAccountFetcher();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final N26AuthenticationController controller =
                new N26AuthenticationController(
                        apiClient,
                        getAgentConfig(),
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        storage);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    private AgentConfiguration<N26Configuration> getAgentConfig() {
        return getAgentConfigurationController().getAgentConfiguration(N26Configuration.class);
    }

    private TransactionalAccountRefreshController initTransactionalAccountFetcher() {
        N26TransactionalAccountFetcher fetcher = new N26TransactionalAccountFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(fetcher)));
    }
}
