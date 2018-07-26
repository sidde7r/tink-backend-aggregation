package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.rpc.Account;
import java.util.List;

public interface SetAccountsToAggregateContext {
    void setAccountsToAggregate(List<Account> accounts);
    List<Account> getCachedAccounts();
    List<String> getOptInAccountNumbers();
}
