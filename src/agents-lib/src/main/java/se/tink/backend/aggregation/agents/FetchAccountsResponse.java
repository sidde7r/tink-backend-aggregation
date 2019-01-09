package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.aggregation.rpc.Account;

public class FetchAccountsResponse {
    private List<Account> accounts;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
