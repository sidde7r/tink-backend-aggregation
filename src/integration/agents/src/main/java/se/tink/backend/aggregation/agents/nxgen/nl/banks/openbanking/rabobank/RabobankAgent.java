package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.RabobankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.SandboxTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.transactional.TransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class RabobankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final RabobankApiClient apiClient;
    private final String clientName;
    private RabobankConfiguration rabobankConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public RabobankAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new RabobankApiClient(client, persistentStorage, request.isManual());
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        // Necessary to circumvent HTTP 413: Payload too large
        client.disableSignatureRequestHeader();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        rabobankConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                RabobankConstants.Market.INTEGRATION_NAME,
                                clientName,
                                RabobankConfiguration.class)
                        .orElseThrow(
                                () -> new IllegalStateException("Rabobank configuration missing."));
        apiClient.setConfiguration(rabobankConfiguration, configuration.getEidasProxy());

        final String password = rabobankConfiguration.getClientSSLKeyPassword();
        final byte[] p12 = rabobankConfiguration.getClientSSLP12bytes();

        client.setSslClientCertificate(p12, password);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new RabobankAuthenticator(
                                apiClient, persistentStorage, rabobankConfiguration));

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
}
