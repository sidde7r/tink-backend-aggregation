package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator.OpenBankProjectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.configuration.OpenBankProjectConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.OpenBankProjectTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.OpenBankProjectTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class OpenBankProjectAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final OpenBankProjectApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public OpenBankProjectAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new OpenBankProjectApiClient(client, sessionStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    public abstract String getIntegrationName();

    public abstract String getClientName();

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final OpenBankProjectConfiguration openBankProjectConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                getIntegrationName(),
                                getClientName(),
                                OpenBankProjectConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                ErrorMessages.MISSING_CONFIGURATION));
        if (!openBankProjectConfiguration.isValid()) {
            throw new IllegalStateException(ErrorMessages.BAD_CONFIGURATION);
        }

        apiClient.setConfiguration(openBankProjectConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OpenBankProjectAuthenticator authenticator =
                new OpenBankProjectAuthenticator(apiClient, sessionStorage);
        return new PasswordAuthenticationController(authenticator);
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
        final OpenBankProjectTransactionalAccountFetcher accountFetcher =
                new OpenBankProjectTransactionalAccountFetcher(apiClient);

        final OpenBankProjectTransactionFetcher transactionFetcher =
                new OpenBankProjectTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                transactionFetcher, OpenBankProjectConstants.Fetcher.START_PAGE)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
