package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.IngSandboxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.configuration.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.IngAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.IngTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.session.IngSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final IngApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        final String market = request.getProvider().getMarket().toLowerCase();
        apiClient = new IngApiClient(client, sessionStorage, market);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final IngConfiguration config =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                IngConstants.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                IngConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                IngConstants.ErrorMessages.MISSING_CONFIGURATION));
        apiClient.setConfiguration(config);

        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(config.getClientKeyStorePath()),
                config.getClientKeyStorePassword());
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new IngSandboxAuthenticator(apiClient, sessionStorage);

        //        TODO switch authenticator for production
        //        final IngAuthenticator authenticator = new IngAuthenticator(apiClient,
        // sessionStorage);
        //        final OAuth2AuthenticationController oAuth2AuthenticationController =
        //                new OAuth2AuthenticationController(
        //                        persistentStorage, supplementalInformationHelper, authenticator);
        //
        //        return new AutoAuthenticationController(
        //                request,
        //                context,
        //                new ThirdPartyAppAuthenticationController<>(
        //                        oAuth2AuthenticationController, supplementalInformationHelper),
        //                oAuth2AuthenticationController);
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
                new IngAccountsFetcher(
                        apiClient, request.getProvider().getCurrency().toUpperCase()),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new IngTransactionsFetcher(apiClient))));
    }

    @Override
    protected IngSessionHandler constructSessionHandler() {
        return new IngSessionHandler();
    }
}
