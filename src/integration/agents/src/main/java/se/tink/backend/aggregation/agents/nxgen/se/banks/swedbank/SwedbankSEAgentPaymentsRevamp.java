package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSELoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankAbstractAgentPaymentsRevamp;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SwedbankSEAgentPaymentsRevamp extends SwedbankAbstractAgentPaymentsRevamp
        implements RefreshLoanAccountsExecutor {
    private final LoanRefreshController loanRefreshController;

    public SwedbankSEAgentPaymentsRevamp(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                request,
                context,
                agentsServiceConfiguration,
                new SwedbankSEConfiguration(request.getProvider().getPayload()),
                new SwedbankSEApiClientProvider());

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SwedbankSELoanFetcher((SwedbankSEApiClient) apiClient));
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }
}
