package se.tink.agent.sdk.fetching.accounts;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

/**
 * A Fetcher for returning SAVINGS accounts.
 *
 * <p>Use this interface, as opposed to {@link CombinedCheckingSavingsAccountsFetcher} if the API
 * you integrate against has separate endpoints for CHECKING and SAVINGS accounts.
 */
public interface SavingsAccountsFetcher {
    List<TransactionalAccount> fetchAccounts();
}
