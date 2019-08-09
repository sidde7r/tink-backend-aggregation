package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.BnpParibasAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.BnpParibasTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.BnpParibasTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BnpParibasBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    private BnpParibasApiBaseClient apiClient;
    private BnpParibasConfiguration bnpParibasConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AgentsServiceConfiguration agentsServiceConfiguration;
    private BnpParibasTransactionalAccountFetcher accountFetcher;
    private BnpParibasTransactionFetcher transactionFetcher;

    public BnpParibasBaseAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair,
            final String bank) {
        super(request, context, signatureKeyPair);
        clientName = request.getProvider().getPayload();
        this.apiClient = new BnpParibasApiBaseClient(client, sessionStorage, bank);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BnpParibasAuthenticator(
                                apiClient, sessionStorage, bnpParibasConfiguration),
                        configuration.getCallbackJwtSignatureKeyPair(),
                        request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    private BnpParibasConfiguration getBnpParibasConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        BnpParibasBaseConstants.INTEGRATION_NAME,
                        clientName,
                        BnpParibasConfiguration.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        BnpParibasBaseConstants.ErrorMessages
                                                .MISSING_CONFIGURATION));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        this.agentsServiceConfiguration = configuration;

        bnpParibasConfiguration = getBnpParibasConfiguration();
        apiClient.setConfiguration(bnpParibasConfiguration);
        client.setEidasProxy(configuration.getEidasProxy(), bnpParibasConfiguration.getEidasQwac());

        accountFetcher.setEidasProxyConfiguration(configuration.getEidasProxy());
        transactionFetcher.setEidasProxyConfiguration(configuration.getEidasProxy());
    }

    public AgentsServiceConfiguration getConfiguration() {
        return this.agentsServiceConfiguration;
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

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        accountFetcher = new BnpParibasTransactionalAccountFetcher(apiClient, sessionStorage);

        transactionFetcher = new BnpParibasTransactionFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }
}
