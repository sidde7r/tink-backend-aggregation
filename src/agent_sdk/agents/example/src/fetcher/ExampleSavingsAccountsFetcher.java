package se.tink.agent.agents.example.fetcher;

import java.util.List;
import se.tink.agent.sdk.fetching.accounts.SavingsAccountsFetcher;
import se.tink.agent.sdk.models.account.SavingsAccount;

public class ExampleSavingsAccountsFetcher implements SavingsAccountsFetcher {
    @Override
    public List<SavingsAccount> fetchAccounts() {
        return null;
    }
}
