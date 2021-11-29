package se.tink.agent.sdk.models.account.builder;

import java.util.List;
import se.tink.agent.sdk.models.account.AccountBalance;

public interface BalancesBuildStep<T> {
    T balance(AccountBalance balance);

    T balances(List<AccountBalance> balances);
}
