package se.tink.agent.agents.example.fetcher;

import java.util.List;
import se.tink.agent.sdk.fetching.accounts.CheckingAccountsFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ExampleCheckingAccountsFetcher implements CheckingAccountsFetcher {

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        return null;
    }
}
