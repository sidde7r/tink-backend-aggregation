package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.AbnAmroAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.AbnAmroAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.AbnAmroTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.session.AbnAmroSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AbnAmroAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final AbnAmroApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AbnAmroConfiguration configuration;

    public AbnAmroAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.apiClient = new AbnAmroApiClient(client, persistentStorage);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final AbnAmroConfiguration abnAmroConfiguration =
                getAgentConfigurationController().getAgentConfiguration(AbnAmroConfiguration.class);

        this.configuration = abnAmroConfiguration;
        apiClient.setConfiguration(abnAmroConfiguration);

        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new AbnAmroAuthenticator(apiClient, persistentStorage, configuration),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new AbnAmroAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new AbnAmroTransactionFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AbnAmroSessionHandler();
    }
}
