package se.tink.backend.aggregation.agents;

import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.AccountFeatures;

public class RefreshLoanAccountsResponse {
    private Map<Account, AccountFeatures> refreshedItems;

    public Map<Account, AccountFeatures> getRefreshedItems() {
        return refreshedItems;
    }

    public void setRefreshedItems(Map<Account, AccountFeatures> refreshedItems) {
        this.refreshedItems = refreshedItems;
    }
}
