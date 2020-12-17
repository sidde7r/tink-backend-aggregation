package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.cofidis;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.loans.SibsLoansFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;

@AgentCapabilities({LOANS})
public final class CofidisAgent extends SibsProgressiveBaseAgent
        implements RefreshLoanAccountsExecutor {

    private final LoanRefreshController loanRefreshController;

    @Inject
    public CofidisAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration configuration) {
        super(agentComponentProvider, configuration);
        SibsLoansFetcher fetcher = new SibsLoansFetcher(apiClient, request, userState);
        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(fetcher)));
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
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }
}
