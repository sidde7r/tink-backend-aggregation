package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.agents.rpc.Account;

public interface SetAccountsToAggregateContext {
    void setAccountsToAggregate(List<Account> accounts);

    List<Account> getCachedAccounts();

    List<String> getUniqueIdOfUserSelectedAccounts();
}
