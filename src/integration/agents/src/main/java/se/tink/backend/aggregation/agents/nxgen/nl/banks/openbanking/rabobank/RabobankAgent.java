package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.ManualOrAutoAuth;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.RabobankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.SandboxTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class RabobankAgent extends SubsequentGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent,
                ManualOrAutoAuth {

    private static final Logger logger = LoggerFactory.getLogger(RabobankAgent.class);

    private final RabobankApiClient apiClient;
    private final String clientName;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final ProgressiveAuthenticator progressiveAuthenticator;
    private final ManualOrAutoAuth manualOrAutoAuthAuthenticator;

    public RabobankAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final AgentsServiceConfiguration agentsConfiguration) {
        super(request, context, agentsConfiguration.getSignatureKeyPair());

        clientName = request.getProvider().getPayload();

        final RabobankConfiguration rabobankConfiguration =
                agentsConfiguration
                        .getIntegrations()
                        .getClientConfiguration(
                                RabobankConstants.Market.INTEGRATION_NAME,
                                clientName,
                                RabobankConfiguration.class)
                        .orElseThrow(
                                () -> new IllegalStateException("Rabobank configuration missing."));

        final String password = rabobankConfiguration.getClientSSLKeyPassword();
        final byte[] p12 = rabobankConfiguration.getClientSSLP12bytes();

        client.setSslClientCertificate(p12, password);
        client.shouldQsealcJwt();
        EidasIdentity eidasIdentity =
                new EidasIdentity(context.getClusterId(), context.getAppId(), RabobankAgent.class);

        apiClient =
                new RabobankApiClient(
                        client,
                        persistentStorage,
                        rabobankConfiguration,
                        agentsConfiguration.getEidasProxy(),
                        eidasIdentity,
                        request.isManual());

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        final OAuth2AuthenticationProgressiveController controller =
                new OAuth2AuthenticationProgressiveController(
                        persistentStorage,
                        new RabobankAuthenticator(
                                apiClient, persistentStorage, rabobankConfiguration),
                        credentials,
                        strongAuthenticationState);

        final AutoAuthenticationProgressiveController autoAuthenticationController =
                new AutoAuthenticationProgressiveController(
                        request,
                        context,
                        new ThirdPartyAppAuthenticationProgressiveController(controller),
                        controller);

        progressiveAuthenticator = autoAuthenticationController;
        manualOrAutoAuthAuthenticator = autoAuthenticationController;
    }

    @Override
    public boolean login() {
        throw new AssertionError(); // ProgressiveAuthAgent::login should always be used
    }

    @Override
    public SteppableAuthenticationResponse login(final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        return ProgressiveAuthController.of(progressiveAuthenticator, credentials).login(request);
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final TransactionDatePaginator<TransactionalAccount> transactionFetcher;

        if (isSandbox()) {
            transactionFetcher = new SandboxTransactionFetcher(apiClient);
        } else {
            transactionFetcher = new TransactionFetcher(apiClient);
        }

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private boolean isSandbox() {
        return clientName.toLowerCase().contains("sandbox");
    }

    @Override
    public boolean isManualAuthentication(final Credentials credentials) {
        return manualOrAutoAuthAuthenticator.isManualAuthentication(credentials);
    }
}
