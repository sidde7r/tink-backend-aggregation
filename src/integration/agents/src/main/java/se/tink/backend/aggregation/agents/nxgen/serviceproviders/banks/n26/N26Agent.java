package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount.N26AccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount.N26TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.session.N26SessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class N26Agent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    protected final N26ApiClient n26APiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public N26Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.n26APiClient = new N26ApiClient(this.client, sessionStorage, persistentStorage);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    public AgentConfigurationControllerable getAgentConfigurationController() {
        return super.getAgentConfigurationController();
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

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
                new N26AccountFetcher(n26APiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new N26TransactionFetcher(n26APiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new N26SessionHandler(n26APiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return Optional.of(n26APiClient.fetchIdentityData())
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
