package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.JyskeBankNemidAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.identity.JyskeIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.JyskeBankAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public class JyskeBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {
    protected final JyskeBankApiClient apiClient;
    private final StatusUpdater statusUpdater;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final JyskeIdentityDataFetcher identityDataFetcher;

    @Inject
    public JyskeBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.setFollowRedirects(false);
        this.apiClient = new JyskeBankApiClient(client, sessionStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.statusUpdater = componentProvider.getContext();
        this.identityDataFetcher =
                new JyskeIdentityDataFetcher(
                        apiClient, new JyskeBankPersistentStorage(persistentStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        JyskeBankNemidAuthenticator jyskeBankNemidAuthenticator =
                new JyskeBankNemidAuthenticator(
                        apiClient,
                        sessionStorage,
                        persistentStorage,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext);

        return new AutoAuthenticationController(
                request, systemUpdater, jyskeBankNemidAuthenticator, jyskeBankNemidAuthenticator);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final JyskeBankAccountFetcher fetcher = new JyskeBankAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(fetcher, 0)));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataFetcher.fetchIdentityData());
    }
}
