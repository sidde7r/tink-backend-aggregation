package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.TargobankSandboxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.configuration.TargobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.configuration.TargobankConfiguration.Environment;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.TargobankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.TargobankTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class TargobankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private final String clientName;
    private final TargobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private TargobankConfiguration targobankConfiguration;

    public TargobankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new TargobankApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final TargobankAccountFetcher accountFetcher = new TargobankAccountFetcher(apiClient);

        final TargobankTransactionFetcher transactionFetcher =
                new TargobankTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        targobankConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                TargobankConstants.INTEGRATION_NAME,
                                clientName,
                                TargobankConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));
        apiClient.setConfiguration(getClientConfiguration());

        //      if (targobankConfiguration.getEnvironment() == Environment.PRODUCTION) {
        //          TODO: Set EidasProxy here
        //      }
    }

    protected TargobankConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        TargobankConstants.INTEGRATION_NAME,
                        clientName,
                        TargobankConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (targobankConfiguration.getEnvironment() == Environment.SANDBOX) {
            return new TargobankSandboxAuthenticator(apiClient, persistentStorage);
        } else {
            // TODO : Put production Authenticator here
            throw new NotImplementedException("Production authenticator is not implemented");
        }
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }
}
