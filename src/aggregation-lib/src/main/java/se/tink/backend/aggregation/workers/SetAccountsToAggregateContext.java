package se.tink.backend.aggregation.workers;

import se.tink.backend.aggregation.rpc.Account;
import java.util.List;

public interface SetAccountsToAggregateContext {
    void setAccounts(List<Account> accounts);
}
