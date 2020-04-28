package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Locale;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSELoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;

public class SwedbankSEAgent extends SwedbankAbstractAgent implements RefreshLoanAccountsExecutor {

    private final LoanRefreshController loanRefreshController;

    @Inject
    public SwedbankSEAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new SwedbankSEConfiguration(
                        componentProvider.getCredentialsRequest().getProvider().getPayload()),
                new SwedbankSEApiClientProvider(),
                new SwedbankDateUtils(ZoneId.of("Europe/Stockholm"), new Locale("sv", "SE")));

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
