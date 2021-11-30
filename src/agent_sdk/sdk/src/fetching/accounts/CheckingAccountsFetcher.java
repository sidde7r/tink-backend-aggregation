package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public interface CheckingAccountsFetcher {
    List<TransactionalAccount> fetchAccounts();
}
