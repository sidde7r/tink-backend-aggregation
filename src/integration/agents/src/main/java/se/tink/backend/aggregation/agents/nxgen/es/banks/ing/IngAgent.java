package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.session.IngSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final IngApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new IngApiClient(client);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new IngAuthenticator(apiClient, sessionStorage);
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

        IngTransactionalAccountFetcher fetcher = new IngTransactionalAccountFetcher(apiClient);

        TransactionMonthPaginationController<TransactionalAccount> paginationController =
                new TransactionMonthPaginationController<>(fetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<TransactionalAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        TransactionalAccountRefreshController refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController, updateController, fetcher, fetcherController);

        return refreshController;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(apiClient, sessionStorage);
    }
}
