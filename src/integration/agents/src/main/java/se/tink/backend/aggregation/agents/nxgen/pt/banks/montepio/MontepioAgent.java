package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.MontepioAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.MontepioTransactionalAccountsFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MontepioAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private final MontepioApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public MontepioAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new MontepioApiClient(client, sessionStorage);
        this.transactionalAccountRefreshController = constructAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        MontepioAuthenticator montepioAuthenticator = new MontepioAuthenticator(apiClient);
        return new PasswordAuthenticationController(montepioAuthenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController constructAccountRefreshController() {
        MontepioTransactionalAccountsFetcher transactionalAccountsFetcher =
                new MontepioTransactionalAccountsFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountsFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                transactionalAccountsFetcher, 1)));
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
