package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.IcaBankenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.IcaBankenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.IcaBankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class IcaBankenAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final IcaBankenApiClient apiClient;
    private final String clientName;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public IcaBankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new IcaBankenApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new IcaBankenAuthenticator(apiClient, sessionStorage);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final IcaBankenConfiguration icaBankenConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                IcaBankenConstants.INTEGRATION_NAME,
                                clientName,
                                IcaBankenConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));

        apiClient.setConfiguration(icaBankenConfiguration);
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
        IcaBankenTransactionalAccountFetcher accountFetcher =
                new IcaBankenTransactionalAccountFetcher(apiClient);

        IcaBankenTransactionFetcher transactionFetcher = new IcaBankenTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
