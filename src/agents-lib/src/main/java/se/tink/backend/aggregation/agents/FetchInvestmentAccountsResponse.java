package se.tink.backend.aggregation.agents;

import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;

public class FetchInvestmentAccountsResponse {
    private final Map<Account, AccountFeatures> accounts;

    public FetchInvestmentAccountsResponse(
            Map<Account, AccountFeatures> accounts) {
        this.accounts = accounts;
    }

    public Map<Account, AccountFeatures> getAccounts() {
        return accounts;
    }
}
