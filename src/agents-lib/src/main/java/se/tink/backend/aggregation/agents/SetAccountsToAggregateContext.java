package se.tink.backend.aggregation.agents;

import se.tink.backend.agents.rpc.Account;
import java.util.List;

public interface SetAccountsToAggregateContext {
    void setAccountsToAggregate(List<Account> accounts);
    List<Account> getCachedAccounts();
    List<String> getUniqueIdOfUserSelectedAccounts();
}
