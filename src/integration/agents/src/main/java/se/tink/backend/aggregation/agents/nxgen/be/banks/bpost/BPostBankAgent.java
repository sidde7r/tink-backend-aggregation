package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account.BPostBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account.BPostBankTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankEntityManager;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({SAVINGS_ACCOUNTS})
public final class BPostBankAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private BPostBankEntityManager entityManager;
    private BPostBankAuthenticator authenticator;
    private BPostBankApiClient apiClient;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public BPostBankAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = new BPostBankApiClient(client);
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        SteppableAuthenticationResponse loginResponse = super.login(request);
        getEntityManager().save();
        return loginResponse;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new BPostBankAuthenticator(
                            apiClient, getEntityManager().getAuthenticationContext(), request);
        }
        return authenticator;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return getTransactionalAccountRefreshController().fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return getTransactionalAccountRefreshController().fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return getTransactionalAccountRefreshController().fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return getTransactionalAccountRefreshController().fetchSavingsTransactions();
    }

    private BPostBankEntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = new BPostBankEntityManager(persistentStorage);
        }
        return entityManager;
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        if (transactionalAccountRefreshController == null) {
            transactionalAccountRefreshController =
                    new TransactionalAccountRefreshController(
                            metricRefreshController,
                            updateController,
                            new BPostBankTransactionalAccountFetcher(
                                    apiClient, getEntityManager().getAuthenticationContext()),
                            createTransactionFetcher());
        }
        return transactionalAccountRefreshController;
    }

    private TransactionFetcherController<TransactionalAccount> createTransactionFetcher() {
        BPostBankTransactionsFetcher transactionFetcher =
                new BPostBankTransactionsFetcher(
                        apiClient, getEntityManager().getAuthenticationContext());
        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(transactionFetcher, 1);
        return new TransactionFetcherController<TransactionalAccount>(
                transactionPaginationHelper, transactionPagePaginationController);
    }
}
