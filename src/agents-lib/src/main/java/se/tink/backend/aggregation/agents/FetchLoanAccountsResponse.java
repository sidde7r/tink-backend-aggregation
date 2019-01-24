package se.tink.backend.aggregation.agents;

import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public class FetchLoanAccountsResponse {
    private final Map<Account, AccountFeatures> accounts;

    public FetchLoanAccountsResponse(
            Map<Account, AccountFeatures> accounts) {
        this.accounts = accounts;
    }

    public Map<Account, AccountFeatures> getAccounts() {
        return accounts;
    }
}
