package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator.SantanderPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderCheckingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderCheckingTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SantanderAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final SantanderApiClient apiClient;

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SantanderApiClient(client, sessionStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new SantanderPasswordAuthenticator(apiClient, sessionStorage));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SantanderCheckingAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new SantanderCheckingTransactionFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        throw new NotImplementedException("session handler not implemented ");
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }
}
