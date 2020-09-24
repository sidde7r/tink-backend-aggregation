package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspSignEncryptFilter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.IspAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.IdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.LoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA, LOANS})
public final class IspAgent extends SubsequentProgressiveGenerationAgent
        implements ProgressiveAuthAgent,
                RefreshCheckingAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final IspApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final IdentityFetcher identityFetcher;

    @Inject
    public IspAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
        applyFilters();
        apiClient = new IspApiClient(client, this.sessionStorage);
        TransactionFetcher transactionFetcher = new TransactionFetcher(apiClient);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(transactionFetcher);
        this.loanRefreshController = constructLoanRefreshController(transactionFetcher);
        this.identityFetcher = new IdentityFetcher(apiClient);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            TransactionFetcher transactionFetcher) {
        TransactionalAccountFetcher transactionalAccountFetcher =
                new TransactionalAccountFetcher(this.apiClient);
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0),
                        null));
    }

    public LoanRefreshController constructLoanRefreshController(
            TransactionFetcher transactionFetcher) {
        LoanAccountFetcher loanAccountFetcher = new LoanAccountFetcher(this.apiClient);
        return new LoanRefreshController(
                this.metricRefreshController,
                this.updateController,
                loanAccountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0),
                        null));
    }

    private void applyFilters() {
        client.addFilter(new IspSignEncryptFilter());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new IspAuthenticator(
                apiClient, supplementalInformationFormer, sessionStorage, persistentStorage);
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
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityFetcher.fetchIdentityData());
    }
}
