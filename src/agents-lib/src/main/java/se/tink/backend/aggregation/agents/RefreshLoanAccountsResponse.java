package se.tink.backend.aggregation.agents;

import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.AccountFeatures;

public class RefreshLoanAccountsResponse {
    private Map<Account, AccountFeatures> accounts;

    public Map<Account, AccountFeatures> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<Account, AccountFeatures> accounts) {
        this.accounts = accounts;
    }
}
