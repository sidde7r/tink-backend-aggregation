package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.SBABAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.identitydata.SBABIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.SBABLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.SBABAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.SBABTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.session.SBABSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({SAVINGS_ACCOUNTS, IDENTITY_DATA, LOANS, MORTGAGE_AGGREGATION})
public final class SBABAgent extends NextGenerationAgent
        implements RefreshSavingsAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {
    private final SBABApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LoanRefreshController loanRefreshController;

    @Inject
    public SBABAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = new SBABApiClient(client, sessionStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController, updateController, new SBABLoanFetcher(apiClient));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new SBABAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
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
    protected SessionHandler constructSessionHandler() {
        return new SBABSessionHandler(apiClient, sessionStorage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        SBABIdentityDataFetcher identityDataFetcher = new SBABIdentityDataFetcher(apiClient);
        return identityDataFetcher.getIdentityDataResponse();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        SBABAccountFetcher accountFetcher = new SBABAccountFetcher(apiClient);
        SBABTransactionFetcher transactionFetcher = new SBABTransactionFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }
}
