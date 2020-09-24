package se.tink.backend.aggregation.agents.agentcapabilities;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;

@Ignore
@AgentCapabilities(generateFromImplementedExecutors = true)
public class TestAgentImplementingExecutors extends BaseTestAgent
        implements RefreshIdentityDataExecutor, RefreshLoanAccountsExecutor {

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return null;
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return null;
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return null;
    }
}
