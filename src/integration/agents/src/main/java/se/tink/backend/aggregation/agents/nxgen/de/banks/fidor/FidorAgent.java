package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.FidorPasswordAutenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.fetcher.transactional.FidorAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.fetcher.transactional.FidorTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.session.FidorSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class FidorAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final FidorApiClient fidorApiClient;
    private final AgentTemporaryStorage agentTemporaryStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FidorAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        fidorApiClient = new FidorApiClient(this.client, persistentStorage);
        agentTemporaryStorage = context.getAgentTemporaryStorage();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new FidorPasswordAutenticator(fidorApiClient, agentTemporaryStorage));
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
        FidorTransactionFetcher transactionFetcher = new FidorTransactionFetcher(fidorApiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new FidorAccountFetcher(fidorApiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0),
                        transactionFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new FidorSessionHandler(this.fidorApiClient);
    }
}
