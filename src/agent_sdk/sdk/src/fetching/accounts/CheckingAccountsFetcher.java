package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.agent.sdk.models.account.CheckingAccount;

public interface CheckingAccountsFetcher {
    List<CheckingAccount> fetchAccounts();
}
