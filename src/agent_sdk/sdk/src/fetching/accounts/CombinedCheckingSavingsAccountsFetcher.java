package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

/**
 * A combined Fetcher for returning both CHECKING and SAVINGS accounts.
 *
 * <p>Use this interface, as opposed to {@link CheckingAccountsFetcher} and/or {@link
 * SavingsAccountsFetcher}, when the API you integrate against return both of these types on the
 * same endpoint.
 */
public interface CombinedCheckingSavingsAccountsFetcher {
    List<TransactionalAccount> fetchAccounts();
}
