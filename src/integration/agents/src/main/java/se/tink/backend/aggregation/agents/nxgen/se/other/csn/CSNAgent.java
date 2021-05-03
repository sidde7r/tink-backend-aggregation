package se.tink.backend.aggregation.agents.nxgen.se.other.csn;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid.CSNBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.LoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.identitydata.CSNIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.session.CSNSessionHandler;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({Capability.LOANS, Capability.IDENTITY_DATA})
public class CSNAgent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor, RefreshIdentityDataExecutor {

    private final CSNAuthSessionStorageHelper authSessionStorageHelper;
    private final CSNApiClient apiClient;
    private final LoanRefreshController loanRefreshController;

    @Inject
    public CSNAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.authSessionStorageHelper = new CSNAuthSessionStorageHelper(sessionStorage);
        this.apiClient = configureApiClient();
        this.loanRefreshController = createLoanRefreshController();
    }

    private CSNApiClient configureApiClient() {
        client.setUserAgent(CSNConstants.HeaderValues.USER_AGENT);
        return new CSNApiClient(client, authSessionStorageHelper);
    }

    private LoanRefreshController createLoanRefreshController() {
        final LoanAccountFetcher loanAccountFetcher = new LoanAccountFetcher(apiClient);
        return new LoanRefreshController(
                metricRefreshController, updateController, loanAccountFetcher);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new CSNBankIdAuthenticator(apiClient, authSessionStorageHelper),
                persistentStorage,
                request);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CSNSessionHandler(apiClient, authSessionStorageHelper);
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
        return new FetchIdentityDataResponse(
                new CSNIdentityDataFetcher(apiClient).fetchIdentityData());
    }
}
