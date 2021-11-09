package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.LhvAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.configuration.LhvConfiguration;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.LhvAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.LhvTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.sessionhandler.LhvSessionHandler;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({Capability.CHECKING_ACCOUNTS, Capability.SAVINGS_ACCOUNTS})
public final class LhvAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final LhvApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LocalDate todaysDate;
    protected AgentConfiguration<LhvConfiguration> agentConfiguration;
    protected LhvConfiguration lhvConfiguration;

    @Inject
    public LhvAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(LhvConfiguration.class);

        this.todaysDate = componentProvider.getLocalDateTimeSource().now().toLocalDate();
        this.apiClient =
                new LhvApiClient(
                        client, agentConfiguration, request, persistentStorage, todaysDate);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LhvSessionHandler(apiClient);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new LhvAuthenticator(
                apiClient,
                credentials,
                persistentStorage,
                supplementalInformationController,
                sessionStorage,
                catalog,
                supplementalInformationHelper,
                strongAuthenticationState);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        LhvAccountFetcher accountFetcher = new LhvAccountFetcher(apiClient, sessionStorage);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new LhvTransactionFetcher(
                                        apiClient, transactionPaginationHelper, todaysDate))));
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }
}
