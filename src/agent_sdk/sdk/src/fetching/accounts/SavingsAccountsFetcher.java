package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.agent.sdk.models.account.SavingsAccount;

public interface SavingsAccountsFetcher {
    List<SavingsAccount> fetchAccounts();
}
