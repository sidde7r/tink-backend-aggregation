package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.SbabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.SbabSandboxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.identity.SbabIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.SbabLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.SbabSavingsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.session.SbabSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SbabAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final SbabApiClient apiClient;
    private final String clientName;

    private final LoanRefreshController loanRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SbabAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SbabApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController, updateController, new SbabLoanFetcher(apiClient));

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        final SbabConfiguration config = getClientConfiguration();

        apiClient.setConfiguration(config);
        sessionStorage.put(StorageKeys.ACCESS_TOKEN, config.getSandboxAccessToken());
    }

    public SbabConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfigurationFromK8s(
                        SbabConstants.INTEGRATION_NAME, clientName, SbabConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final Environment environment = getClientConfiguration().getEnvironment();

        if (environment == Environment.SANDBOX) {
            return new SbabSandboxAuthenticator();
        }

        return new ThirdPartyAppAuthenticationController<>(
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new SbabAuthenticator(apiClient, sessionStorage),
                        credentials,
                        strongAuthenticationState),
                supplementalInformationHelper);
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
        final SbabSavingsAccountFetcher fetcher = new SbabSavingsAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(fetcher)));
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SbabSessionHandler(apiClient, sessionStorage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        SbabIdentityFetcher fetcher = new SbabIdentityFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
