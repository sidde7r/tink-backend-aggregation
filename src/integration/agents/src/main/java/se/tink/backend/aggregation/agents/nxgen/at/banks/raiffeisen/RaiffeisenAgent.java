package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.RaiffeisenPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.RaiffeisenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.RaiffeisenTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.session.RaiffeisenSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class RaiffeisenAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final RaiffeisenWebApiClient apiClient;
    private final RaiffeisenSessionStorage raiffeisenSessionStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public RaiffeisenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.apiClient = new RaiffeisenWebApiClient(this.client, request.getProvider());
        this.raiffeisenSessionStorage = new RaiffeisenSessionStorage(sessionStorage);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new RaiffeisenPasswordAuthenticator(apiClient, raiffeisenSessionStorage));
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new RaiffeisenTransactionalAccountFetcher(apiClient, raiffeisenSessionStorage),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new RaiffeisenTransactionFetcher(
                                        apiClient, raiffeisenSessionStorage))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new RaiffeisenSessionHandler(apiClient, raiffeisenSessionStorage);
    }
}
