package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.SocieteGeneraleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
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

public final class SocieteGeneraleAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final SocieteGeneraleApiClient apiClient;
    private SocieteGeneraleConfiguration societeGeneraleConfiguration;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AutoAuthenticationController authenticator;
    private SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher;
    private SocieteGeneraleTransactionalAccountFetcher accountFetcher;
    private SocieteGeneraleTransactionFetcher transactionFetcher;
    private SignatureHeaderProvider signatureHeaderProvider;

    public SocieteGeneraleAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        apiClient = new SocieteGeneraleApiClient(client, persistentStorage);
        apiClient.setConfiguration(societeGeneraleConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        signatureHeaderProvider = new SignatureHeaderProvider();

        transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(
                        agentsServiceConfiguration.getEidasProxy());
        societeGeneraleIdentityDataFetcher =
                new SocieteGeneraleIdentityDataFetcher(
                        apiClient,
                        societeGeneraleConfiguration,
                        sessionStorage,
                        signatureHeaderProvider,
                        agentsServiceConfiguration.getEidasProxy(),
                        getEidasIdentity());

        societeGeneraleConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SocieteGeneraleConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new SocieteGeneraleAuthenticator(
                                apiClient, persistentStorage, societeGeneraleConfiguration),
                        credentials,
                        strongAuthenticationState);

        authenticator =
                new AutoAuthenticationController(
                        request,
                        context,
                        new ThirdPartyAppAuthenticationController<>(
                                controller, supplementalInformationHelper),
                        controller);

        return authenticator;
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            EidasProxyConfiguration eidasProxyConfiguration) {
        accountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(
                        apiClient,
                        societeGeneraleConfiguration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        getEidasIdentity());

        transactionFetcher =
                new SocieteGeneraleTransactionFetcher(
                        apiClient,
                        societeGeneraleConfiguration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        getEidasIdentity());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return societeGeneraleIdentityDataFetcher.response();
    }
}
